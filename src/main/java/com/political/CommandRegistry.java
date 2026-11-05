package com.political;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.util.Prediction;
import com.political.bounty.BountyGui;
import com.political.bounty.SlayerData;
import com.political.bounty.SlayerItems;
import com.political.bounty.SlayerManager;
import com.political.economy.AuctionHouseGui;
import com.political.economy.AuctionHouseManager;
import com.political.economy.BankGui;
import com.political.economy.CoinManager;
import com.political.economy.CryptoMarket;
import com.political.economy.ShopItemGui;
import com.political.economy.ShopManager;
import com.political.economy.StockMarket;
import com.political.economy.UndergroundAuctionGui;
import com.political.economy.UndergroundAuctionManager;
import com.political.government.ChairGui;
import com.political.government.DictatorManager;
import com.political.government.ElectionManager;
import com.political.government.JudgeGui;
import com.political.government.Perk;
import com.political.government.PerkManager;
import com.political.government.PerksGui;
import com.political.government.ViceChairGui;
import com.political.government.VoteGui;
import com.political.party.PartyJoinGui;
import com.political.party.PartyLeaderGui;
import com.political.party.PoliticalPartyManager;
import com.political.recipe.RecipeConfigManager;
import com.political.recipe.RecipeEditorGui;
import com.political.recipe.RecipesGui;
import com.political.recipe.SlayerRecipes;
import com.political.technique.TechniqueGui;
import com.political.technique.TechniqueManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import net.minecraft.commands.arguments.*;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.inventory.MenuType;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import java.util.*;

import java.util.List;
import java.util.Map;
import com.political.bounty.SlayerManager.SlayerType;

public class CommandRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandRegistry.class);


    public static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerVote(dispatcher);
        registerRun(dispatcher);
        registerTechnique(dispatcher);
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
        registerCrafterContract(dispatcher);
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
        registerCivilAntiCheatAndViewCommands(dispatcher);

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
        registerPoliticalPartyCommands(dispatcher);
        registerLeaderCommand(dispatcher);
        
        // /tree command - Progression Tree system
        registerTree(dispatcher);
        registerAscensionCommand(dispatcher);
        registerSectorHeartsCommand(dispatcher);
        registerSyndicateCommand(dispatcher);
        registerFishing(dispatcher);

        // /hunt command - Hunts system
        registerHunt(dispatcher);

        // /worldboss command - World Bosses system
        registerWorldBoss(dispatcher);

        // /bosses command - Spawn non-slayer bosses for coins
        registerBosses(dispatcher);

        // /warp command - Dimension warps for 30M coins
        registerWarp(dispatcher);

        // Magic gameplay is left unregistered on purpose.

        // /skills command - Skills Hub (HOTM-style)
        dispatcher.register(Commands.literal("skills")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    SkillsGui.openMainMenu(player);
                    return 1;
                }));

        // /mastery and /hotm — open Mastery GUI
        dispatcher.register(Commands.literal("mastery")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    MasteryGui.openMainMenu(player);
                    return 1;
                }));

        dispatcher.register(Commands.literal("hotm")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ClassGui.openMainMenu(player);
                    return 1;
                }));
        dispatcher.register(Commands.literal("hotg")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    AscensionGrindGui.openMain(player);
                    return 1;
                }));
        dispatcher.register(Commands.literal("hots")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    AscensionHeartsGui.openTreePicker(player);
                    return 1;
                }));

        dispatcher.register(Commands.literal("menu")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    MenuGui.openMainMenu(player);
                    return 1;
                }));

        dispatcher.register(Commands.literal("enhancements")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    MasteryGui.openEnhancementsGui(player);
                    return 1;
                }));

        dispatcher.register(Commands.literal("partycommissions")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    PartyJoinGui.openPartyCommissionsGui(player);
                    return 1;
                }));
        dispatcher.register(Commands.literal("hotf")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ProgressionTreeGui.openMainMenu(player);
                    return 1;
                }));

        // /class command - Classes System
        registerClass(dispatcher);

        // /setrecipe and /customitems are registered below with GUI support

        dispatcher.register(Commands.literal("bank")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    BankGui.open(player);
                    return 1;
                }));

        // Setrecipe command (admin only - GUI based)
        dispatcher.register(Commands.literal("setrecipe")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS)) // Admin only
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    try {
                        RecipeEditorGui.openRecipeList(player);
                    } catch (Exception e) {
                        LOGGER.error("/setrecipe failed to open the recipe list for player {}", player.getUUID(), e);
                        player.sendSystemMessage(Component.literal("An unexpected error occurred: " + e.getClass().getSimpleName() + ": " + e.getMessage()).withStyle(ChatFormatting.RED), false);
                        StackTraceElement[] st = e.getStackTrace();
                        if (st != null) {
                            for (StackTraceElement el : st) {
                                String cn = el.getClassName();
                                if (cn != null && cn.startsWith("com.political")) {
                                    player.sendSystemMessage(Component.literal("Location: " + el.toString()).withStyle(ChatFormatting.GRAY), false);
                                    break;
                                }
                            }
                        }
                    }
                    return 1;
                })
        );

        // Customitems command - opens CustomItemsGui (admin only)
        dispatcher.register(Commands.literal("customitems")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS)) // Admin only
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CustomItemsGui.open(player);
                    return 1;
                }));

        // Fletchingadmin command (admin only - for editing arrow recipes)
        dispatcher.register(Commands.literal("fletchingadmin")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS)) // Admin only
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    FletchingTableGui.openAdminGui(player);
                    return 1;
                }));

        // Texturehelper command - accessible to all players
        dispatcher.register(Commands.literal("texturehelper")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    TextureHelperGui.openMainMenu(player);
                    return 1;
                })
                .then(Commands.literal("txt")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            exportTextureCodesTxt(player);
                            return 1;
                        })));

        // /recipe — browser for all players; editor & toggles for ops (/recipe edit, list, enable, disable)
        dispatcher.register(Commands.literal("recipe")
                .requires(src -> true)
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    RecipeConfigManager.logRecipeFileStatusForRecipeCommand();
                    try {
                        RecipesGui.openMainMenu(player);
                        player.sendSystemMessage(Component.literal("✓ Recipe browser opened — use the book for the full JSON index.").withStyle(ChatFormatting.GREEN), false);
                    } catch (Exception e) {
                        player.sendSystemMessage(Component.literal("Error: " + e.getMessage()).withStyle(ChatFormatting.RED), false);
                        LOGGER.error("/recipe failed to open the recipe browser for player {}", player.getUUID(), e);
                    }
                    return 1;
                })
                .then(Commands.literal("edit")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            RecipeEditorGui.openRecipeList(player);
                            return 1;
                        }))
                .then(Commands.literal("list")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            RecipeEditorGui.openRecipeList(player);
                            return 1;
                        }))
                .then(Commands.literal("view")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    String name = StringArgumentType.getString(context, "name");

                                    for (SlayerRecipes.Recipe recipe : RecipeConfigManager.getRecipes()) {
                                        if (recipe.name.equalsIgnoreCase(name)) {
                                            player.sendSystemMessage(Component.literal("═══════════════════════════════════")
                                                    .withStyle(ChatFormatting.GOLD), false);
                                            player.sendSystemMessage(Component.literal("       " + recipe.name)
                                                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
                                            player.sendSystemMessage(Component.literal("═══════════════════════════════════")
                                                    .withStyle(ChatFormatting.GOLD), false);

                                            player.sendSystemMessage(Component.literal("§7Result: §f" + recipe.result.getHoverName().getString() + " §7x" + recipe.result.getCount()), false);
                                            player.sendSystemMessage(Component.literal(""), false);

                                            player.sendSystemMessage(Component.literal("§6Ingredients:"), false);
                                            if (recipe.ingredients != null) {
                                                for (ItemStack ing : recipe.ingredients) {
                                                    if (ing != null && !ing.isEmpty()) {
                                                        player.sendSystemMessage(Component.literal("  §f" + ing.getHoverName().getString() + " §7x" + ing.getCount()), false);
                                                    }
                                                }
                                            }

                                            boolean enabled = RecipeConfigManager.isRecipeEnabled(recipe.name);
                                            player.sendSystemMessage(Component.literal(""), false);
                                            player.sendSystemMessage(Component.literal("§7Status: " + (enabled ? "§aENABLED" : "§cDISABLED")), false);
                                            return 1;
                                        }
                                    }
                                    player.sendSystemMessage(Component.literal("§cRecipe not found: " + name), false);
                                    return 0;
                                })))
                .then(Commands.literal("enable")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    String name = StringArgumentType.getString(context, "name");

                                    for (SlayerRecipes.Recipe recipe : RecipeConfigManager.getRecipes()) {
                                        if (recipe.name.equalsIgnoreCase(name)) {
                                            if (!RecipeConfigManager.isRecipeEnabled(recipe.name)) {
                                                RecipeConfigManager.toggleRecipe(recipe.name);
                                            }
                                            player.sendSystemMessage(Component.literal("§aRecipe '" + recipe.name + "' enabled!"), false);
                                            return 1;
                                        }
                                    }
                                    player.sendSystemMessage(Component.literal("§cRecipe not found: " + name), false);
                                    return 0;
                                })))
                .then(Commands.literal("disable")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    String name = StringArgumentType.getString(context, "name");

                                    for (SlayerRecipes.Recipe recipe : RecipeConfigManager.getRecipes()) {
                                        if (recipe.name.equalsIgnoreCase(name)) {
                                            if (RecipeConfigManager.isRecipeEnabled(recipe.name)) {
                                                RecipeConfigManager.toggleRecipe(recipe.name);
                                            }
                                            player.sendSystemMessage(Component.literal("§cRecipe '" + recipe.name + "' disabled!"), false);
                                            return 1;
                                        }
                                    }
                                    player.sendSystemMessage(Component.literal("§cRecipe not found: " + name), false);
                                    return 0;
                                }))));

        // Debug command to list recipes (admin only)
        dispatcher.register(Commands.literal("debugrecipes")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(context -> {
                    List<SlayerRecipes.Recipe> recipes = RecipeConfigManager.getRecipes();
                    context.getSource().sendSuccess(() -> Component.literal("Found " + recipes.size() + " recipes:"), false);
                    for (SlayerRecipes.Recipe recipe : recipes) {
                        String customId = SlayerItems.getCustomItemId(recipe.result);
                        String name = recipe.result.get(DataComponents.CUSTOM_NAME) != null ? 
                            recipe.result.get(DataComponents.CUSTOM_NAME).getString() : "Unknown";
                        context.getSource().sendSuccess(() -> 
                            Component.literal("- " + recipe.name + " -> " + name + " (ID: " + customId + ")"), false);
                    }
                    return 1;
                }));

        // /report command - Report system for players
        dispatcher.register(Commands.literal("report")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ReportGui.openMainMenu(player);
                    return 1;
                }));

        // /casino command - Coin sink system (gambling, lottery, etc.)
        dispatcher.register(Commands.literal("casino")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CoinSinkManager.openCoinSinkGui(player);
                    return 1;
                })
        );

        dispatcher.register(Commands.literal("treasury")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    RoyalTreasuryManager.open(player);
                    return 1;
                }));

        // /upgrades command - Themed upgrades GUI hub
        dispatcher.register(Commands.literal("upgrades")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    UpgradesGui.openMainGui(player);
                    return 1;
                })
        );

        // /cca command - CivilCraft Achievements (short alias)
        dispatcher.register(Commands.literal("cca")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CivilCraftAchievements.openAchievementsGui(player);
                    return 1;
                })
        );

        // /civilcraftachievements command
        dispatcher.register(Commands.literal("civilcraftachievements")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CivilCraftAchievements.openAchievementsGui(player);
                    return 1;
                })
        );

        // /civilachievement command
        dispatcher.register(Commands.literal("civilachievement")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CivilCraftAchievements.openAchievementsGui(player);
                    return 1;
                })
        );

        // /daily command - Daily rewards
        dispatcher.register(Commands.literal("daily")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    DailyRewards.openGui(player);
                    return 1;
                })
        );

        // /dailyrewards command (alias)
        dispatcher.register(Commands.literal("dailyrewards")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    DailyRewards.openGui(player);
                    return 1;
                })
        );

        // /lottery command - Quick access to lottery
        dispatcher.register(Commands.literal("lottery")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    player.sendSystemMessage(Component.literal("§6═══════════════════════════════════"), false);
                    player.sendSystemMessage(Component.literal("§6🎰 LOTTERY §6").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
                    player.sendSystemMessage(Component.literal("§ePot: §6" + String.format("%,d", CoinSinkManager.getLotteryPot()) + " coins"), false);
                    player.sendSystemMessage(Component.literal("§aYour tickets: §f" + CoinSinkManager.getPlayerTickets(player.getUUID().toString())), false);
                    player.sendSystemMessage(Component.literal("§7Ticket cost: §f" + String.format("%,d", 10000) + " coins"), false);
                    long timeLeft = CoinSinkManager.getTimeUntilNextDraw();
                    int hours = (int) (timeLeft / (60 * 60 * 1000));
                    int mins = (int) ((timeLeft % (60 * 60 * 1000)) / (60 * 1000));
                    player.sendSystemMessage(Component.literal("§7Next draw in: §f" + hours + "h " + mins + "m"), false);
                    player.sendSystemMessage(Component.literal("§6═══════════════════════════════════"), false);
                    player.sendSystemMessage(Component.literal("§eUse /casino to buy tickets!"), false);
                    return 1;
                })
        );

        // /broadcast command - Global broadcast for 10M coins
        dispatcher.register(Commands.literal("broadcast")
                .requires(src -> true)
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            String message = StringArgumentType.getString(context, "message");
                            CoinSinkManager.purchaseGlobalBroadcast(player, message);
                            return 1;
                        }))
        );

        // /renameitem command - Rename held item for 5M coins
        dispatcher.register(Commands.literal("renameitem")
                .requires(src -> true)
                .then(Commands.argument("name", StringArgumentType.greedyString())
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            String name = StringArgumentType.getString(context, "name");
                            CoinSinkManager.purchaseItemRename(player, name);
                            return 1;
                        }))
        );

        // /bounty opens the menu, /bounty cancel drops the active quest. These were two registrations of the
        // same root; they are one now (M1.8), with the same effect Brigadier's merge had.
        dispatcher.register(Commands.literal("bounty")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    BountyGui.openMainMenu(player);  // Changed from SlayerGui
                    return 1;
                })
                .then(Commands.literal("cancel")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            if (SlayerManager.hasActiveQuest(player)) {
                                SlayerManager.cancelQuest(player);
                            } else {
                                player.sendSystemMessage(Component.literal("✖ No active bounty!")
                                        .withStyle(ChatFormatting.RED), false);
                            }
                            return 1;
                        })
                )
        );

        // /stocks command - Stock Market access
        dispatcher.register(Commands.literal("stocks")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    StockMarketGui.openMainMenu(player);
                    return 1;
                })
                .then(Commands.literal("buy")
                        .then(Commands.argument("symbol", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    for (StockMarket.Stock stock : StockMarket.Stock.values()) {
                                        builder.suggest(stock.symbol);
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            String symbol = StringArgumentType.getString(context, "symbol").toUpperCase();
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            StockMarket.buyStock(player, symbol, amount);
                                            return 1;
                                        }))))
                .then(Commands.literal("sell")
                        .then(Commands.argument("symbol", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    for (StockMarket.Stock stock : StockMarket.Stock.values()) {
                                        builder.suggest(stock.symbol);
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            String symbol = StringArgumentType.getString(context, "symbol").toUpperCase();
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            StockMarket.sellStock(player, symbol, amount);
                                            return 1;
                                        }))))
                .then(Commands.literal("portfolio")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            StockMarketGui.openPortfolio(player, 0);
                            return 1;
                        }))
                .then(Commands.literal("movers")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            StockMarketGui.openTopMovers(player);
                            return 1;
                        }))
                .then(Commands.literal("news")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            StockMarketGui.openMarketNews(player);
                            return 1;
                        }))
        );

        // Admin stock commands
        dispatcher.register(Commands.literal("stockadmin")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    StockAdminGui.openMainMenuFrom(player, null);
                    return 1;
                })
                .then(Commands.literal("event")
                        .executes(context -> {
                            StockMarket.triggerRandomEvent();
                            context.getSource().sendSuccess(() -> 
                                    Component.literal("Triggered random market event!").withStyle(ChatFormatting.YELLOW), true);
                            return 1;
                        }))
                .then(Commands.literal("reset")
                        .executes(context -> {
                            StockMarket.initializeMarket();
                            context.getSource().sendSuccess(() -> 
                                    Component.literal("Reset all stock prices to base values!").withStyle(ChatFormatting.GREEN), true);
                            return 1;
                        }))
        );

        // /crypto command - Crypto Market access
        dispatcher.register(Commands.literal("crypto")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CryptoMarketGui.openMainMenu(player);
                    return 1;
                })
                .then(Commands.literal("buy")
                        .then(Commands.argument("symbol", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    for (CryptoMarket.Crypto crypto : CryptoMarket.Crypto.values()) {
                                        builder.suggest(crypto.symbol);
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            String symbol = StringArgumentType.getString(context, "symbol").toUpperCase();
                                            double amount = DoubleArgumentType.getDouble(context, "amount");
                                            CryptoMarket.buyCrypto(player, symbol, amount);
                                            return 1;
                                        }))))
                .then(Commands.literal("sell")
                        .then(Commands.argument("symbol", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    for (CryptoMarket.Crypto crypto : CryptoMarket.Crypto.values()) {
                                        builder.suggest(crypto.symbol);
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            String symbol = StringArgumentType.getString(context, "symbol").toUpperCase();
                                            double amount = DoubleArgumentType.getDouble(context, "amount");
                                            CryptoMarket.sellCrypto(player, symbol, amount);
                                            return 1;
                                        }))))
                .then(Commands.literal("wallet")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            CryptoMarketGui.openWallet(player);
                            return 1;
                        }))
                .then(Commands.literal("stake")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            CryptoMarketGui.openStaking(player);
                            return 1;
                        }))
                .then(Commands.literal("swap")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            CryptoMarketGui.openSwapMenu(player);
                            return 1;
                        })));

        // Admin crypto commands
        dispatcher.register(Commands.literal("cryptoadmin")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CryptoAdminGui.openMainMenuFrom(player, null);
                    return 1;
                })
                .then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("symbol", StringArgumentType.string())
                                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                                .executes(context -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                                    String symbol = StringArgumentType.getString(context, "symbol").toUpperCase();
                                                    double amount = DoubleArgumentType.getDouble(context, "amount");
                                                    CryptoMarket.deposit(target.getUUID().toString(), symbol, amount);
                                                    context.getSource().sendSuccess(() -> 
                                                            Component.literal("Gave " + amount + " " + symbol + " to " + target.getName().getString())
                                                                    .withStyle(ChatFormatting.GREEN), true);
                                                    return 1;
                                                }))))));

        registerShopPriceCommands(dispatcher);
        registerVanish(dispatcher);
        registerSetTitle(dispatcher);
        registerSummonChild(dispatcher);
    }

        // Helper method for stats display
        private static void showSlayerStats(ServerPlayer viewer, ServerPlayer target) {
            String uuid = target.getUUID().toString();

            viewer.sendSystemMessage(Component.literal(""), false);
            viewer.sendSystemMessage(Component.literal("══════════════════════════════")
                    .withStyle(ChatFormatting.GOLD), false);
            viewer.sendSystemMessage(Component.literal("  ⚔ " + target.getName().getString() + "'s Bounty Stats ⚔")
                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD), false);
            viewer.sendSystemMessage(Component.literal("══════════════════════════════")
                    .withStyle(ChatFormatting.GOLD), false);

            for (SlayerType type : SlayerType.values()) {
                int level = SlayerData.getSlayerLevel(uuid, type);
                int bosses = SlayerData.getBossesKilled(uuid, type);
                viewer.sendSystemMessage(Component.literal("  " + type.displayName + ": ")
                        .withStyle(type.color)
                        .append(Component.literal("Level " + level).withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" (" + bosses + " bosses)").withStyle(ChatFormatting.GRAY)), false);
            }

            viewer.sendSystemMessage(Component.literal(""), false);
            viewer.sendSystemMessage(Component.literal("  Total Level: " + SlayerData.getTotalSlayerLevel(uuid))
                    .withStyle(ChatFormatting.AQUA), false);
            viewer.sendSystemMessage(Component.literal("══════════════════════════════")
                    .withStyle(ChatFormatting.GOLD), false);
        }




    private static void registerVote(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vote")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) {
                        return 0;
                    }

                    if (PoliticalPartyManager.isLeaderElectionActive()) {
                        PartyLeaderElectionGui.open(player);
                    } else if (ElectionManager.isElectionActive()) {
                        VoteGui.open(player);
                    } else {
                        player.sendSystemMessage(Component.literal("No ruling-party or chair election is active.").withStyle(ChatFormatting.RED));
                    }
                    return 1;
                }));
    }

    private static void registerRun(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("run")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    ElectionManager.nominateSelf(player);
                    return 1;
                }));
    }

    private static void registerTechnique(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("technique")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    TechniqueGui.open(player);
                    return 1;
                })
                .then(Commands.literal("learn")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            if (player == null) return 0;
                            TechniqueManager.learnFromHeldManual(player);
                            return 1;
                        }))
                .then(Commands.literal("equip")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayer();
                                    if (player == null) return 0;
                                    TechniqueManager.equip(player, StringArgumentType.getString(context, "name"));
                                    return 1;
                                })))
                .then(Commands.literal("ranks")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            if (player == null) return 0;
                            TechniqueManager.showRanks(player);
                            return 1;
                        }))
                .then(Commands.literal("cast")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            if (player == null) return 0;
                            TechniqueManager.castSelected(player);
                            return 1;
                        })));
    }

    private static void registerForceUndergroundAuction(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("forceundergroundauction")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(context -> {
                    UndergroundAuctionManager.forceStartAuction(PoliticalServer.server);
                    context.getSource().sendSuccess(() ->
                            Component.literal("✓ Underground auction started!").withStyle(ChatFormatting.LIGHT_PURPLE), true);
                    return 1;
                })
        );

        // Shorter alias
        dispatcher.register(Commands.literal("forceauction")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(context -> {
                    UndergroundAuctionManager.forceStartAuction(PoliticalServer.server);
                    context.getSource().sendSuccess(() ->
                            Component.literal("✓ Underground auction started!").withStyle(ChatFormatting.LIGHT_PURPLE), true);
                    return 1;
                })
        );
// ============ BALTOP COMMAND ============
        dispatcher.register(Commands.literal("baltop")
                .executes(ctx -> {
                    Map<String, Long> allCoins = DataManager.getData().playerCoins;
                    Map<String, Integer> allCredits = DataManager.getData().playerCredits;
                    Map<String, Long> allBankMain = DataManager.getData().playerBankMain;
                    Map<String, Long> allBankSavings = DataManager.getData().playerBankSavings;

                    // Combine into total wealth (credits * 1,000 + coins + bankMain + bankSavings)
                    Map<String, Long> totalWealth = new java.util.HashMap<>();

                    for (Map.Entry<String, Long> entry : allCoins.entrySet()) {
                        totalWealth.put(entry.getKey(), entry.getValue());
                    }

                    for (Map.Entry<String, Integer> entry : allCredits.entrySet()) {
                        String uuid = entry.getKey();
                        long creditValue = entry.getValue() * 1000L;
                        totalWealth.merge(uuid, creditValue, (oldVal, newVal) -> oldVal + newVal);
                    }

                    for (Map.Entry<String, Long> entry : allBankMain.entrySet()) {
                        String uuid = entry.getKey();
                        totalWealth.merge(uuid, entry.getValue(), (oldVal, newVal) -> oldVal + newVal);
                    }

                    for (Map.Entry<String, Long> entry : allBankSavings.entrySet()) {
                        String uuid = entry.getKey();
                        totalWealth.merge(uuid, entry.getValue(), (oldVal, newVal) -> oldVal + newVal);
                    }

                    // Sort by wealth descending (no limit)
                    List<Map.Entry<String, Long>> sorted = totalWealth.entrySet().stream()
                            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                            .toList();

                    ctx.getSource().sendSystemMessage(Component.literal("=== Wealth Leaderboard ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

                    int rank = 1;
                    for (Map.Entry<String, Long> entry : sorted) {
                        String name = DataManager.getPlayerName(entry.getKey());
                        long total = entry.getValue();

                        ctx.getSource().sendSystemMessage(Component.literal(
                                "#" + rank + " " + name + ": " + String.format("%,d", total) + " coins (total)"
                        ).withStyle(rank == 1 ? ChatFormatting.YELLOW : (rank <= 3 ? ChatFormatting.WHITE : ChatFormatting.GRAY)));
                        rank++;
                    }

                    if (sorted.isEmpty()) {
                        ctx.getSource().sendSystemMessage(Component.literal("No players with wealth yet!").withStyle(ChatFormatting.GRAY));
                    }

                    return 1;
                })
        );

    }

    private static void registerAuctionHouse(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ah")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    AuctionHouseGui.openFrom(player, null);
                    return 1;
                })
                .then(Commands.literal("sell")
                        .then(Commands.argument("price", IntegerArgumentType.integer(1, 1000000))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayer();
                                    if (player == null) return 0;

                                    int price = IntegerArgumentType.getInteger(context, "price");
                                    ItemStack heldItem = player.getMainHandItem();

                                    if (heldItem.isEmpty()) {
                                        player.sendSystemMessage(Component.literal("Hold an item in your main hand!").withStyle(ChatFormatting.RED));
                                        return 0;
                                    }

                                    int listingTax = (int) AuctionHouseManager.calculateListingTax(price);
                                    if (listingTax > 0 && !CreditItem.hasCredits(player, listingTax)) {
                                        player.sendSystemMessage(Component.literal("Not enough credits for listing tax (" + listingTax + ")!").withStyle(ChatFormatting.RED));
                                        return 0;
                                    }

                                    if (listingTax > 0) {
                                        CreditItem.removeCredits(player, listingTax);
                                    }

                                    ItemStack toSell = heldItem.copyWithCount(heldItem.getCount());
                                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

                                    AuctionHouseManager.AuctionListing listing = new AuctionHouseManager.AuctionListing(
                                            player.getUUID().toString(),
                                            player.getName().getString(),
                                            toSell,
                                            price
                                    );
                                    AuctionHouseManager.addListing(listing);

                                    player.sendSystemMessage(Component.literal("Listed " + toSell.getHoverName().getString() + " for " + price + " credits!" + (listingTax > 0 ? " (Tax: " + listingTax + ")" : "")).withStyle(ChatFormatting.GREEN));
                                    return 1;
                                }))));
    }


    private static void registerIntercom(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("intercom")
                .then(Commands.argument("message", com.mojang.brigadier.arguments.StringArgumentType.greedyString())
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            if (player == null) return 0;

                            String chairUuid = DataManager.getChair();
                            if (chairUuid == null || !player.getUUID().toString().equals(chairUuid)) {
                                player.sendSystemMessage(Component.literal("§cOnly the current Chair can use /intercom!"), false);
                                return 0;
                            }

                            String message = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "message");
                            String chairName = player.getName().getString();
                            String border = "§8╔══════════════════════════════╗";
                            String footerLine = "§8╚══════════════════════════════╝";
                            String titleLine = "§8║  §6§l📢 INTERCOM §8— §f" + chairName;
                            String msgLine   = "§8║  §7" + message;
                            for (ServerPlayer online : PoliticalServer.server.getPlayerList().getPlayers()) {
                                online.sendSystemMessage(Component.literal(border), false);
                                online.sendSystemMessage(Component.literal(titleLine), false);
                                online.sendSystemMessage(Component.literal(msgLine), false);
                                online.sendSystemMessage(Component.literal(footerLine), false);
                            }
                            return 1;
                        })));
    }

    private static void registerSpawn(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spawn")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    SpawnManager.teleportToSpawn(player);
                    return 1;
                }));

        dispatcher.register(Commands.literal("setspawn")
                .requires(source -> {
                    ServerPlayer player = source.getPlayer();
                    return player != null && PoliticalServer.isOperator(player);
                })
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    SpawnManager.setSpawn(player);
                    return 1;
                }));
    }
    /**
     * /coins. Two registrations of this root used to exist (here and in registerForceUndergroundAuction);
     * they are one now (M1.8) with the effect Brigadier's merge gave them: the bare command shows the
     * caller's coins, add/remove/set keep the requirement of this (the first) registration, which is a
     * player who is an operator, and run the quiet admin executors of the second one.
     */
    private static void registerCoins(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("coins")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    int coins = CoinManager.getCoins(player);
                    player.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.YELLOW));
                    player.sendSystemMessage(Component.literal("Your Coins: " + coins).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                    player.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.YELLOW));
                    return 1;
                })
                .then(Commands.literal("add")
                        .requires(source -> {
                            ServerPlayer player = source.getPlayer();
                            return player != null && PoliticalServer.isOperator(player);
                        })
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            CoinManager.giveCoinsQuiet(target, amount);
                                            ctx.getSource().sendSystemMessage(Component.literal("Added " + amount + " coins to " + target.getName().getString()).withStyle(ChatFormatting.GREEN));
                                            target.sendSystemMessage(Component.literal("+" + amount + " coins (admin)").withStyle(ChatFormatting.YELLOW));
                                            return 1;
                                        }))))
                .then(Commands.literal("remove")
                        .requires(source -> {
                            ServerPlayer player = source.getPlayer();
                            return player != null && PoliticalServer.isOperator(player);
                        })
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            int current = CoinManager.getCoins(target);
                                            CoinManager.setCoins(target.getUUID().toString(), Math.max(0, current - amount));
                                            DataManager.save(PoliticalServer.server);
                                            ctx.getSource().sendSystemMessage(Component.literal("Removed " + amount + " coins from " + target.getName().getString()).withStyle(ChatFormatting.YELLOW));
                                            return 1;
                                        }))))
                .then(Commands.literal("set")
                        .requires(source -> {
                            ServerPlayer player = source.getPlayer();
                            return player != null && PoliticalServer.isOperator(player);
                        })
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            CoinManager.setCoins(target.getUUID().toString(), amount);
                                            DataManager.save(PoliticalServer.server);
                                            ctx.getSource().sendSystemMessage(Component.literal("Set " + target.getName().getString() + "'s coins to " + amount).withStyle(ChatFormatting.GREEN));
                                            return 1;
                                        })))));
    }

    private static void registerCrafterContract(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("craftercontract")
                .executes(context -> {
                    ServerPlayer p = context.getSource().getPlayerOrException();
                    p.sendSystemMessage(Component.literal("Use /craftercontract buy | claim | abandon").withStyle(ChatFormatting.GRAY), false);
                    return 1;
                })
                .then(Commands.literal("buy")
                        .executes(context -> {
                            ServerPlayer p = context.getSource().getPlayerOrException();
                            CrafterContractManager.tryPurchase(p);
                            return 1;
                        }))
                .then(Commands.literal("claim")
                        .executes(context -> {
                            ServerPlayer p = context.getSource().getPlayerOrException();
                            CrafterContractManager.tryClaim(p);
                            return 1;
                        }))
                .then(Commands.literal("abandon")
                        .executes(context -> {
                            ServerPlayer p = context.getSource().getPlayerOrException();
                            CrafterContractManager.abandon(p);
                            return 1;
                        })));
    }

    private static void registerShop(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("shop")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    ShopGui.openMainMenuFrom(player, null);
                    return 1;
                }));

        dispatcher.register(Commands.literal("sell")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    ShopGui.openSellInventory(player);
                    return 1;
                }));
    }

    private static void registerStore(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("store")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    
                    // Non-functional store command - just shows a message
                    player.sendSystemMessage(
                        Component.literal("🛒 Store")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                            .append(Component.literal("\nThe online store is currently under construction.")
                                .withStyle(ChatFormatting.GRAY))
                            .append(Component.literal("\n\nVisit our website at: ")
                                .withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal("play.civilcraft.xyz")
                                .withStyle(ChatFormatting.AQUA, ChatFormatting.UNDERLINE))
                            .append(Component.literal("\n\n")
                                .withStyle(ChatFormatting.RESET))
                            .append(Component.literal("Note: The store is not yet functional.")
                                .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC))
                    );
                    return 1;
                }));
    }

    private static void registerUndergroundAuction(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /bid <amount> - anyone can use
        dispatcher.register(Commands.literal("bid")
                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            if (player == null) return 0;
                            long amount = LongArgumentType.getLong(context, "amount");
                            UndergroundAuctionManager.placeBid(player, amount);
                            return 1;
                        })));

        // /auction - anyone can use (or restrict to op if you want)
        dispatcher.register(Commands.literal("auction")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    UndergroundAuctionGui.openFrom(player, null);
                    return 1;
                }));

        // /uah - shortcut for players to open underground auction (public)
        dispatcher.register(Commands.literal("uah")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    UndergroundAuctionGui.openFrom(player, null);
                    return 1;
                }));

        // ADMIN ONLY: /placeundergroundauctioneer
        dispatcher.register(Commands.literal("placeundergroundauctioneer")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ServerLevel world = context.getSource().getLevel();
                    UndergroundAuctionManager.spawnAuctioneer(world, player.getX(), player.getY(), player.getZ(), player.getYRot());
                    context.getSource().sendSuccess(() -> Component.literal("✓ Spawned Underground Auctioneer!").withStyle(ChatFormatting.LIGHT_PURPLE), true);
                    return 1;
                }));

        // ADMIN ONLY: /removeundergroundauctioneer
        dispatcher.register(Commands.literal("removeundergroundauctioneer")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    var entities = player.level().getEntitiesOfClass(
                            net.minecraft.world.entity.npc.villager.Villager.class,
                            player.getBoundingBox().inflate(5),
                            UndergroundAuctionManager::isAuctioneer
                    );
                    if (!entities.isEmpty()) {
                        UndergroundAuctionManager.removeAuctioneer(entities.get(0));
                        context.getSource().sendSuccess(() -> Component.literal("✓ Removed Underground Auctioneer!").withStyle(ChatFormatting.GREEN), true);
                    } else {
                        context.getSource().sendSuccess(() -> Component.literal("No Underground Auctioneer nearby!").withStyle(ChatFormatting.RED), false);
                    }
                    return 1;
                }));

        // ADMIN ONLY: /startundergroundauction. A second registration with identical behavior and a looser
        // "player who is an operator" requirement was dropped in M1.8; this one's requirement was the one in force.
        dispatcher.register(Commands.literal("startundergroundauction")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(context -> {
                    UndergroundAuctionManager.startAuction(PoliticalServer.server);
                    context.getSource().sendSuccess(() -> Component.literal("✓ Started Underground Auction!").withStyle(ChatFormatting.LIGHT_PURPLE), true);
                    return 1;
                }));

        // ADMIN ONLY: /endundergroundauction
        dispatcher.register(Commands.literal("endundergroundauction")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(context -> {
                    UndergroundAuctionManager.forceEndAuction();
                    context.getSource().sendSuccess(() -> Component.literal("✓ Ended Underground Auction!").withStyle(ChatFormatting.RED), true);
                    return 1;
                }));
    }

    private static void registerHome(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("home")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    HomeManager.teleportHome(player);
                    return 1;
                }));

        dispatcher.register(Commands.literal("sethome")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    HomeManager.setHome(player);
                    return 1;
                }));
    }

    private static void registerPlaceAuctionMaster(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("placeauctionmaster")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ServerLevel world = context.getSource().getLevel();
                    AuctionMasterManager.spawnAuctionMaster(world, player.getX(), player.getY(), player.getZ(), player.getYRot());
                    context.getSource().sendSuccess(() -> Component.literal("✓ Spawned Auction Master!").withStyle(ChatFormatting.GREEN), true);
                    return 1;
                })
                .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                        .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                        .executes(context -> {
                                            double x = DoubleArgumentType.getDouble(context, "x");
                                            double y = DoubleArgumentType.getDouble(context, "y");
                                            double z = DoubleArgumentType.getDouble(context, "z");
                                            ServerLevel world = context.getSource().getLevel();
                                            AuctionMasterManager.spawnAuctionMaster(world, x, y, z, 0);
                                            context.getSource().sendSuccess(() -> Component.literal("✓ Spawned Auction Master!").withStyle(ChatFormatting.GREEN), true);
                                            return 1;
                                        })
                                        .then(Commands.argument("facing", FloatArgumentType.floatArg())
                                                .executes(context -> {
                                                    double x = DoubleArgumentType.getDouble(context, "x");
                                                    double y = DoubleArgumentType.getDouble(context, "y");
                                                    double z = DoubleArgumentType.getDouble(context, "z");
                                                    float facing = FloatArgumentType.getFloat(context, "facing");
                                                    ServerLevel world = context.getSource().getLevel();
                                                    AuctionMasterManager.spawnAuctionMaster(world, x, y, z, facing);
                                                    context.getSource().sendSuccess(() -> Component.literal("✓ Spawned Auction Master!").withStyle(ChatFormatting.GREEN), true);
                                                    return 1;
                                                })
                                        )
                                )
                        )
                )
        );
    }

    private static void registerImpeachment(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("impeachment")
                .then(Commands.literal("start")
                        .executes(context -> {
                            ServerPlayer source = context.getSource().getPlayer();
                            if (source == null) return 0;

                            String sourceUuid = source.getUUID().toString();

                            if (!DictatorManager.hasJudgePermissions(sourceUuid) && !PoliticalServer.isOperator(source)) {
                                source.sendSystemMessage(Component.literal("Only the Judge can start impeachment!").withStyle(ChatFormatting.RED));
                                return 0;
                            }

                            if (DataManager.getChair() == null) {
                                source.sendSystemMessage(Component.literal("No Chair to impeach!").withStyle(ChatFormatting.RED));
                                return 0;
                            }

                            if (ElectionManager.isImpeachmentActive()) {
                                source.sendSystemMessage(Component.literal("Impeachment vote already active!").withStyle(ChatFormatting.RED));
                                return 0;
                            }

                            ElectionManager.startImpeachment(PoliticalServer.server);
                            return 1;
                        }))
                .then(Commands.literal("vote")
                        .then(Commands.literal("yes")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayer();
                                    if (player == null) return 0;

                                    if (!ElectionManager.isImpeachmentActive()) {
                                        player.sendSystemMessage(Component.literal("No active impeachment vote!").withStyle(ChatFormatting.RED));
                                        return 0;
                                    }

                                    ElectionManager.castImpeachVote(player, true);
                                    return 1;
                                }))
                        .then(Commands.literal("no")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayer();
                                    if (player == null) return 0;

                                    if (!ElectionManager.isImpeachmentActive()) {
                                        player.sendSystemMessage(Component.literal("No active impeachment vote!").withStyle(ChatFormatting.RED));
                                        return 0;
                                    }

                                    ElectionManager.castImpeachVote(player, false);
                                    return 1;
                                }))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            if (player == null) return 0;

                            if (ElectionManager.isImpeachmentActive()) {
                                ImpeachmentGui.open(player);
                            } else {
                                player.sendSystemMessage(Component.literal("No active impeachment vote!").withStyle(ChatFormatting.RED));
                            }
                            return 1;
                        })));
    }

    private static void registerDictator(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dictator")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    String uuid = player.getUUID().toString();

                    if (!DictatorManager.isDictator(uuid)) {
                        player.sendSystemMessage(Component.literal("Only the Dictator can use this command!").withStyle(ChatFormatting.RED));
                        return 0;
                    }
                    DictatorGui.open(player);
                    return 1;
                })
                .then(Commands.literal("add")
                        .requires(source -> {
                            ServerPlayer player = source.getPlayer();
                            return player != null && PoliticalServer.isOperator(player);
                        })
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                    String targetUuid = target.getUUID().toString();
                                    String chair = DataManager.getChair();

                                    if (!targetUuid.equals(chair)) {
                                        context.getSource().sendSystemMessage(Component.literal("Player must be the current Chair to become Dictator!").withStyle(ChatFormatting.RED));
                                        return 0;
                                    }

                                    DictatorManager.setDictator(target);
                                    context.getSource().sendSystemMessage(Component.literal("Set " + target.getName().getString() + " as DICTATOR!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
                                    return 1;
                                })))
                .then(Commands.literal("remove")
                        .requires(source -> {
                            ServerPlayer player = source.getPlayer();
                            return player != null && PoliticalServer.isOperator(player);
                        })
                        .executes(context -> {
                            if (!DictatorManager.isDictatorActive()) {
                                context.getSource().sendSystemMessage(Component.literal("No active dictator!").withStyle(ChatFormatting.RED));
                                return 0;
                            }

                            DictatorManager.removeDictator();
                            context.getSource().sendSystemMessage(Component.literal("Dictatorship removed!").withStyle(ChatFormatting.GREEN));
                            return 1;
                        }))
                .then(Commands.literal("accept")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            if (player == null) return 0;
                            DictatorManager.acceptDictatorOffer(player);
                            return 1;
                        }))
                .then(Commands.literal("decline")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            if (player == null) return 0;
                            DictatorManager.declineDictatorOffer(player);
                            return 1;
                        })));
    }

    private static void registerTakeLeadership(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("takeleadership")
                .then(Commands.literal("force")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            if (player == null) return 0;
                            DictatorManager.takeLeadershipByForce(player);
                            return 1;
                        }))
                .then(Commands.literal("election")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            if (player == null) return 0;
                            DictatorManager.takeLeadershipByElection(player);
                            return 1;
                        })));
    }

    private static void registerSmite(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("smite")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer source = context.getSource().getPlayer();
                            if (source == null) return 0;

                            String sourceUuid = source.getUUID().toString();

                            if (!DictatorManager.isDictator(sourceUuid)
                                    && !DictatorManager.hasJudgePermissions(sourceUuid)
                                    && !PoliticalServer.isOperator(source)) {
                                source.sendSystemMessage(Component.literal("Only the Dictator or Judge can use /smite!").withStyle(ChatFormatting.RED));
                                return 0;
                            }

                            if (!DictatorManager.canSmite()) {
                                long remaining = DictatorManager.getSmiteCooldownRemaining();
                                source.sendSystemMessage(Component.literal("Smite on cooldown! " + remaining + "s remaining").withStyle(ChatFormatting.RED));
                                return 0;
                            }

                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                            DictatorManager.smitePlayer(source, target);
                            return 1;
                        })));
    }

    private static void registerPerks(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("perks")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) {
                        return 0;
                    }

                    String uuid = player.getUUID().toString();
                    String chair = DataManager.getChair();
                    String viceChair = DataManager.getViceChair();

                    boolean isChair = uuid.equals(chair);
                    boolean isViceChair = uuid.equals(viceChair);
                    boolean hasBackdoor = PoliticalServer.isOperator(player);

                    if (!isChair && !isViceChair && !hasBackdoor) {
                        player.sendSystemMessage(Component.literal("Only the Chair or Vice Chair can use this command!").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    if (!PerkManager.canChangePerks(isChair)) {
                        player.sendSystemMessage(Component.literal("Perks have already been selected for this term!").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    PerksGui.open(player, isChair);
                    return 1;
                }));
    }

    private static void registerGov(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("gov")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) {
                        return 0;
                    }

                    if (DictatorManager.isDictatorActive()) {
                        player.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.DARK_RED));
                        player.sendSystemMessage(Component.literal("       ⚠ GOVERNMENT STATUS ⚠").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                        player.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.DARK_RED));

                        player.sendSystemMessage(Component.literal("Dictator: ").withStyle(ChatFormatting.RED)
                                .append(Component.literal(DictatorManager.getDictatorName()).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)));

                        if (DictatorManager.isDictatorTaxEnabled()) {
                            player.sendSystemMessage(Component.literal("Daily Tax: " + DictatorManager.getDictatorTaxAmount() + " credits").withStyle(ChatFormatting.RED));
                        }

                        player.sendSystemMessage(Component.literal(""));
                        player.sendSystemMessage(Component.literal("Elections: SUSPENDED").withStyle(ChatFormatting.DARK_RED));

                        player.sendSystemMessage(Component.literal(""));
                        List<String> perks = PerkManager.getActivePerks();
                        if (!perks.isEmpty()) {
                            player.sendSystemMessage(Component.literal("Active Perks:").withStyle(ChatFormatting.RED));
                            for (String perkId : perks) {
                                Perk perk = PerkManager.getPerk(perkId);
                                if (perk != null) {
                                    player.sendSystemMessage(Component.literal(" • " + perk.name).withStyle(ChatFormatting.DARK_RED));
                                }
                            }
                        }

                        player.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.DARK_RED));
                    } else {
                        player.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.GOLD));
                        player.sendSystemMessage(Component.literal("       GOVERNMENT STATUS").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
                        player.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.GOLD));

                        String chair = DataManager.getChair();
                        String viceChair = DataManager.getViceChair();
                        String judge = DataManager.getJudge();

                        if (chair != null) {
                            player.sendSystemMessage(Component.literal("Chair: ").withStyle(ChatFormatting.GRAY)
                                    .append(Component.literal(DataManager.getPlayerName(chair)).withStyle(ChatFormatting.GREEN)));
                        } else {
                            player.sendSystemMessage(Component.literal("Chair: ").withStyle(ChatFormatting.GRAY)
                                    .append(Component.literal("None").withStyle(ChatFormatting.RED)));
                        }

                        if (viceChair != null) {
                            player.sendSystemMessage(Component.literal("Vice Chair: ").withStyle(ChatFormatting.GRAY)
                                    .append(Component.literal(DataManager.getPlayerName(viceChair)).withStyle(ChatFormatting.AQUA)));
                        } else {
                            player.sendSystemMessage(Component.literal("Vice Chair: ").withStyle(ChatFormatting.GRAY)
                                    .append(Component.literal("None").withStyle(ChatFormatting.RED)));
                        }

                        if (judge != null) {
                            player.sendSystemMessage(Component.literal("Judge: ").withStyle(ChatFormatting.GRAY)
                                    .append(Component.literal(DataManager.getPlayerName(judge)).withStyle(ChatFormatting.LIGHT_PURPLE)));
                        } else {
                            player.sendSystemMessage(Component.literal("Judge: ").withStyle(ChatFormatting.GRAY)
                                    .append(Component.literal("None").withStyle(ChatFormatting.RED)));
                        }

                        player.sendSystemMessage(Component.literal(""));
                        if (ElectionManager.isElectionActive()) {
                            long remaining = ElectionManager.getRemainingTime();
                            String time = PoliticalServer.formatTime(remaining);
                            player.sendSystemMessage(Component.literal("⚡ ELECTION ACTIVE - " + time + " remaining!").withStyle(ChatFormatting.YELLOW));
                        } else if (ElectionManager.isElectionSystemEnabled() && !ElectionManager.isElectionSystemPaused()) {
                            long remaining = ElectionManager.getTimeUntilNextElection();
                            String time = PoliticalServer.formatTime(remaining);
                            player.sendSystemMessage(Component.literal("Next election in: " + time).withStyle(ChatFormatting.GRAY));
                        } else if (!ElectionManager.isElectionSystemEnabled()) {
                            player.sendSystemMessage(Component.literal("Elections: DISABLED").withStyle(ChatFormatting.GRAY));
                        } else if (ElectionManager.isElectionSystemPaused()) {
                            player.sendSystemMessage(Component.literal("Elections: PAUSED").withStyle(ChatFormatting.GRAY));
                        }

                        player.sendSystemMessage(Component.literal(""));
                        List<String> perks = PerkManager.getActivePerks();
                        if (!perks.isEmpty()) {
                            player.sendSystemMessage(Component.literal("Active Perks:").withStyle(ChatFormatting.GOLD));
                            for (String perkId : perks) {
                                Perk perk = PerkManager.getPerk(perkId);
                                if (perk != null) {
                                    player.sendSystemMessage(Component.literal(" • " + perk.name).withStyle(ChatFormatting.WHITE));
                                }
                            }
                        }

                        player.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.GOLD));
                    }
                    return 1;
                }));
    }

    /**
     * /judge. Two registrations of this root used to exist (here and in registerRoleCommands); they are one
     * now (M1.8) with the effect Brigadier's merge gave them: "/judge <player>" appoints the judge, and the bare
     * "/judge" opens the judge's screen. The requirement of this (the first) registration, LEVEL_OWNERS, covers
     * both forms, so today a Judge who is not an operator cannot open the screen. That is a defect
     * (HANDOFF.md, KNOWN ISSUES) kept as it was; it is fixed in M4 with a test.
     */
    private static void registerJudge(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("judge")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    String uuid = player.getUUID().toString();

                    if (DictatorManager.isDictator(uuid)) {
                        player.sendSystemMessage(Component.literal("Dictators must use /dictator instead!").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    if (!uuid.equals(DataManager.getJudge())) {
                        player.sendSystemMessage(Component.literal("Only the Judge can use this command!").withStyle(ChatFormatting.RED));
                        return 0;
                    }
                    JudgeGui.open(player);
                    return 1;
                })
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                            DataManager.setJudge(target.getUUID().toString());
                            DataManager.save(PoliticalServer.server);

                            context.getSource().sendSystemMessage(Component.literal("Set " + target.getName().getString() + " as Judge!").withStyle(ChatFormatting.GREEN));
                            target.sendSystemMessage(Component.literal("You have been appointed as the Server Judge!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                            return 1;
                        })));
    }

    private static void registerExile(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("exile")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer source = context.getSource().getPlayer();
                            if (source == null) return 0;

                            String sourceUuid = source.getUUID().toString();

                            if (!DictatorManager.hasJudgePermissions(sourceUuid)
                                    && !PoliticalServer.isOperator(source)) {
                                source.sendSystemMessage(Component.literal("Only the Judge can use this command!")
                                        .withStyle(ChatFormatting.RED));
                                return 0;
                            }

                            ServerPlayer target = EntityArgument.getPlayer(context, "player");

                            Random rand = new Random();
                            double distance = 10_000 + rand.nextDouble() * 90_000;
                            double angle = rand.nextDouble() * Math.PI * 2;

                            double x = Math.cos(angle) * distance;
                            double z = Math.sin(angle) * distance;

                            ServerLevel world = PoliticalServer.server.overworld();
                            int safeY = findSafeY(world, (int) x, (int) z);

                            PlayerTeleportHelper.teleport(target, world, x, safeY, z, 0, 0);
                            BlockPos blockPos = BlockPos.containing(x, safeY, z);
                            GlobalPos globalPos = GlobalPos.of(world.dimension(), blockPos);
                            LevelData.RespawnData respawnData = new LevelData.RespawnData(globalPos, 0f, 0f);
                            target.setRespawnPosition(new ServerPlayer.RespawnConfig(respawnData, true), false);
                            target.sendSystemMessage(Component.literal("You have been EXILED!")
                                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                            target.sendSystemMessage(Component.literal("Your spawn has been set to this location.")
                                    .withStyle(ChatFormatting.GRAY));

                            for (ServerPlayer p : context.getSource().getServer().getPlayerList().getPlayers()) {
                                p.sendSystemMessage(Component.literal("⚖ " + target.getName().getString() + " has been exiled by the Judge!")
                                        .withStyle(ChatFormatting.RED));
                            }
                            return 1;
                        })));
    }



    private static int findSafeY(ServerLevel world, int x, int z) {
        for (int y = world.getMaxY(); y > world.getMinY(); y--) {
            net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(x, y, z);
            net.minecraft.core.BlockPos below = pos.below();

            if (world.getBlockState(below).isSolid()
                    && world.getBlockState(pos).isAir()
                    && world.getBlockState(pos.above()).isAir()) {
                return y;
            }
        }
        return 100;
    }

    private static void registerRelocate(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("relocate")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer source = context.getSource().getPlayer();
                            if (source == null) return 0;

                            String sourceUuid = source.getUUID().toString();

                            if (!DictatorManager.isDictator(sourceUuid) && !PoliticalServer.isOperator(source)) {
                                source.sendSystemMessage(Component.literal("Only the Dictator can use /relocate!").withStyle(ChatFormatting.RED));
                                return 0;
                            }

                            ServerPlayer target = EntityArgument.getPlayer(context, "player");

                            Random rand = new Random();
                            double distance = 100 + rand.nextDouble() * 900;
                            double angle = rand.nextDouble() * Math.PI * 2;

                            double newX = target.getX() + Math.cos(angle) * distance;
                            double newZ = target.getZ() + Math.sin(angle) * distance;

                            ServerLevel world = (ServerLevel) target.level();
                            int safeY = findSafeY(world, (int) newX, (int) newZ);

                            PlayerTeleportHelper.teleport(target, world, newX, safeY, newZ, target.getYRot(), target.getXRot());

                            target.sendSystemMessage(Component.literal("You have been relocated by the Dictator!")
                                    .withStyle(ChatFormatting.RED));
                            source.sendSystemMessage(Component.literal("Relocated " + target.getName().getString() + " ~" + (int)distance + " blocks away!")
                                    .withStyle(ChatFormatting.GREEN));

                            return 1;
                        })));
    }

    private static void registerImprison(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("imprison")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("time", IntegerArgumentType.integer(1, 120))
                                .then(Commands.argument("location", Vec3Argument.vec3())
                                        .executes(context -> {
                                            ServerPlayer source = context.getSource().getPlayer();
                                            if (source == null) {
                                                return 0;
                                            }

                                            String sourceUuid = source.getUUID().toString();

                                            if (!DictatorManager.hasJudgePermissions(sourceUuid) && !PoliticalServer.isOperator(source)) {
                                                source.sendSystemMessage(Component.literal("Only the Judge can use this command!").withStyle(ChatFormatting.RED));
                                                return 0;
                                            }

                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            int time = IntegerArgumentType.getInteger(context, "time");
                                            Vec3 loc = Vec3Argument.getVec3(context, "location");

                                            PrisonManager.imprison(target, time, loc.x, loc.y, loc.z);

                                            for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
                                                p.sendSystemMessage(Component.literal("⚖ " + target.getName().getString() + " has been imprisoned for " + time + " minutes!").withStyle(ChatFormatting.RED));
                                            }
                                            return 1;
                                        })))));
    }

    private static void registerImpeach(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("impeach")
                .executes(context -> {
                    ServerPlayer source = context.getSource().getPlayer();
                    if (source == null) {
                        return 0;
                    }

                    String sourceUuid = source.getUUID().toString();
                    String judge = DataManager.getJudge();

                    if (!sourceUuid.equals(judge) && !PoliticalServer.isOperator(source)) {
                        source.sendSystemMessage(Component.literal("Only the Judge can use this command!").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    if (DataManager.getChair() == null) {
                        source.sendSystemMessage(Component.literal("No Chair to impeach!").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    if (ElectionManager.isImpeachmentActive()) {
                        source.sendSystemMessage(Component.literal("Impeachment vote already active!").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    ElectionManager.startImpeachment(PoliticalServer.server);
                    return 1;
                }));
    }

    private static void registerForceElection(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("forceelection")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(context -> {
                    ElectionManager.forceStartElection(PoliticalServer.server);
                    context.getSource().sendSystemMessage(Component.literal("Forced election start!").withStyle(ChatFormatting.GREEN));
                    return 1;
                }));
    }

    private static void registerElectionControl(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("election")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .then(Commands.literal("enable")
                        .executes(context -> {
                            ElectionManager.setElectionSystemEnabled(true);
                            ElectionManager.setElectionSystemPaused(false);
                            context.getSource().sendSystemMessage(Component.literal("Election system enabled!").withStyle(ChatFormatting.GREEN));

                            if (ElectionManager.getTimeUntilNextElection() <= 0 && !ElectionManager.isElectionActive()) {
                                ElectionManager.forceStartElection(PoliticalServer.server);
                            }
                            return 1;
                        }))
                .then(Commands.literal("disable")
                        .executes(context -> {
                            ElectionManager.setElectionSystemEnabled(false);
                            context.getSource().sendSystemMessage(Component.literal("Election system disabled!").withStyle(ChatFormatting.RED));
                            return 1;
                        }))
                .then(Commands.literal("pause")
                        .executes(context -> {
                            ElectionManager.setElectionSystemPaused(true);
                            context.getSource().sendSystemMessage(Component.literal("Election system paused!").withStyle(ChatFormatting.YELLOW));
                            return 1;
                        }))
                .then(Commands.literal("play")
                        .executes(context -> {
                            ElectionManager.setElectionSystemPaused(false);
                            context.getSource().sendSystemMessage(Component.literal("Election system resumed!").withStyle(ChatFormatting.GREEN));
                            return 1;
                        })));
    }

    private static void registerForceCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("force")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .then(Commands.literal("chair")
                        .then(Commands.literal("resetperks")
                                .executes(context -> {
                                    PerkManager.onNewTermStart();
                                    PerkManager.setPreviousTermPerks(new ArrayList<>());
                                    context.getSource().sendSystemMessage(Component.literal("Perks reset! All members of government can now select perks again.").withStyle(ChatFormatting.GREEN));
                                    DataManager.save(PoliticalServer.server);
                                    return 1;
                                }))
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .executes(context -> {
                                    var entries = GameProfileArgument.getGameProfiles(context, "player");
                                    if (entries.isEmpty()) {
                                        context.getSource().sendSystemMessage(Component.literal("Player not found!").withStyle(ChatFormatting.RED));
                                        return 0;
                                    }
                                    var entry = entries.iterator().next();

                                    String uuid = entry.id().toString();
                                    String name = entry.name();

                                    DataManager.setChair(uuid);
                                    DataManager.setChairTermCount(1);
                                    DataManager.save(PoliticalServer.server);

                                    context.getSource().sendSystemMessage(Component.literal("Forced " + name + " as Chair!").withStyle(ChatFormatting.GREEN));

                                    ServerPlayer target = PoliticalServer.server.getPlayerList().getPlayer(entry.id());
                                    if (target != null) {
                                        target.sendSystemMessage(Component.literal("You have been appointed as Chair!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                                    }
                                    return 1;
                                })))
                .then(Commands.literal("vicechair")
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .then(Commands.literal("resetperks")
                                        .executes(context -> {
                                            PerkManager.onNewTermStart();
                                            PerkManager.setPreviousTermPerks(new ArrayList<>());
                                            context.getSource().sendSystemMessage(Component.literal("Perks reset! All members of government can now select perks again.").withStyle(ChatFormatting.GREEN));
                                            DataManager.save(PoliticalServer.server);
                                            return 1;
                                        }))
                                .executes(context -> {
                                    var entries = GameProfileArgument.getGameProfiles(context, "player");
                                    if (entries.isEmpty()) {
                                        context.getSource().sendSystemMessage(Component.literal("Player not found!").withStyle(ChatFormatting.RED));
                                        return 0;
                                    }
                                    var entry = entries.iterator().next();

                                    String uuid = entry.id().toString();
                                    String name = entry.name();

                                    DataManager.setViceChair(uuid);
                                    DataManager.save(PoliticalServer.server);

                                    context.getSource().sendSystemMessage(Component.literal("Forced " + name + " as Vice Chair!").withStyle(ChatFormatting.GREEN));

                                    ServerPlayer target = PoliticalServer.server.getPlayerList().getPlayer(entry.id());
                                    if (target != null) {
                                        target.sendSystemMessage(Component.literal("You have been appointed as Vice Chair!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                                    }
                                    return 1;
                                })))
                .then(Commands.literal("judge")
                        .then(Commands.literal("resetperks")
                                .executes(context -> {
                                    PerkManager.onNewTermStart();
                                    PerkManager.setPreviousTermPerks(new ArrayList<>());
                                    context.getSource().sendSystemMessage(Component.literal("Perks reset! All members of government can now select perks again.").withStyle(ChatFormatting.GREEN));
                                    DataManager.save(PoliticalServer.server);
                                    return 1;
                                }))
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .executes(context -> {
                                    var entries = GameProfileArgument.getGameProfiles(context, "player");
                                    if (entries.isEmpty()) {
                                        context.getSource().sendSystemMessage(Component.literal("Player not found!").withStyle(ChatFormatting.RED));
                                        return 0;
                                    }
                                    var entry = entries.iterator().next();

                                    String uuid = entry.id().toString();
                                    String name = entry.name();

                                    DataManager.setJudge(uuid);
                                    DataManager.save(PoliticalServer.server);

                                    context.getSource().sendSystemMessage(Component.literal("Forced " + name + " as Judge!").withStyle(ChatFormatting.GREEN));

                                    ServerPlayer target = PoliticalServer.server.getPlayerList().getPlayer(entry.id());
                                    if (target != null) {
                                        target.sendSystemMessage(Component.literal("You have been appointed as Judge!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                                    }
                                    return 1;
                                })))
        );
    }

    /**
     * /credits. Two registrations of this root used to exist (here and in registerForceUndergroundAuction);
     * they are one now (M1.8) with the effect Brigadier's merge gave them: the whole command needs
     * LEVEL_OWNERS (the first registration's requirement), the bare command and balance show the caller's
     * balance, add/remove/set run the quiet admin versions, and check shows another player's balance.
     */
    private static void registerCredits(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("credits")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    int credits = CreditItem.countCredits(player);
                    player.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.GOLD));
                    player.sendSystemMessage(Component.literal("Your Balance: " + credits + " credits").withStyle(ChatFormatting.GREEN));
                    player.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.GOLD));
                    return 1;
                })
                .then(Commands.literal("balance")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            if (player == null) return 0;
                            int credits = CreditItem.countCredits(player);
                            player.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.GOLD));
                            player.sendSystemMessage(Component.literal("Your Balance: " + credits + " credits").withStyle(ChatFormatting.GREEN));
                            player.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.GOLD));
                            return 1;
                        }))
                .then(Commands.literal("add")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            CreditItem.giveCreditsQuiet(target, amount);
                                            ctx.getSource().sendSystemMessage(Component.literal("Added " + amount + " credits to " + target.getName().getString()).withStyle(ChatFormatting.GREEN));
                                            target.sendSystemMessage(Component.literal("+" + amount + " credits (admin)").withStyle(ChatFormatting.GREEN));
                                            return 1;
                                        }))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            int current = CreditItem.countCredits(target);
                                            CreditItem.setCredits(target, Math.max(0, current - amount));
                                            ctx.getSource().sendSystemMessage(Component.literal("Removed " + amount + " credits from " + target.getName().getString()).withStyle(ChatFormatting.YELLOW));
                                            return 1;
                                        }))))
                .then(Commands.literal("set")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            CreditItem.setCredits(target, amount);
                                            ctx.getSource().sendSystemMessage(Component.literal("Set " + target.getName().getString() + "'s credits to " + amount).withStyle(ChatFormatting.GREEN));
                                            return 1;
                                        }))))
                .then(Commands.literal("check")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    int credits = CreditItem.countCredits(target);
                                    ctx.getSource().sendSystemMessage(Component.literal(target.getName().getString() + " has " + credits + " credits").withStyle(ChatFormatting.GOLD));
                                    return 1;
                                })))
        );
    }

    private static void registerTax(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tax")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    TaxGui.openFrom(player, null);
                    return 1;
                }));
    }

    private static void registerRoleCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("chair")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    String uuid = player.getUUID().toString();

                    if (DictatorManager.isDictator(uuid)) {
                        player.sendSystemMessage(Component.literal("Dictators must use /dictator instead!").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    if (!uuid.equals(DataManager.getChair())) {
                        player.sendSystemMessage(Component.literal("Only the Chair can use this command!").withStyle(ChatFormatting.RED));
                        return 0;
                    }
                    ChairGui.open(player);
                    return 1;
                }));

        dispatcher.register(Commands.literal("vicechair")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    String uuid = player.getUUID().toString();

                    if (DictatorManager.isDictator(uuid)) {
                        player.sendSystemMessage(Component.literal("Dictators must use /dictator instead!").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    if (!uuid.equals(DataManager.getViceChair())) {
                        player.sendSystemMessage(Component.literal("Only the Vice Chair can use this command!").withStyle(ChatFormatting.RED));
                        return 0;
                    }
                    ViceChairGui.open(player);
                    return 1;
                }));

        dispatcher.register(Commands.literal("admin")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    AdminPanel.open(player);
                    return 1;
                }));

        // /givecore command - give custom crafting core (T1/T2/T3) (admin only)
        dispatcher.register(Commands.literal("givecore")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS)) // Admin only
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    SlayerItems.giveCore(player, SlayerManager.SlayerType.ZOMBIE);
                    return 1;
                })
                .then(Commands.literal("t1")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ItemStack core = SlayerItems.createCustomCrafterCoreT1();
                            if (!player.addItem(core)) {
                                player.drop(core, false, Prediction.SERVER_ONLY);
                            }
                            player.sendSystemMessage(Component.literal("✔ Received Custom Crafting Core T1!").withStyle(ChatFormatting.GREEN), false);
                            return 1;
                        }))
                .then(Commands.literal("t2")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ItemStack core = SlayerItems.createCustomCrafterCoreT2();
                            if (!player.addItem(core)) {
                                player.drop(core, false, Prediction.SERVER_ONLY);
                            }
                            player.sendSystemMessage(Component.literal("✔ Received Custom Crafting Core T2!").withStyle(ChatFormatting.AQUA), false);
                            return 1;
                        }))
                .then(Commands.literal("t3")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ItemStack core = SlayerItems.createCustomCrafterCoreT3();
                            if (!player.addItem(core)) {
                                player.drop(core, false, Prediction.SERVER_ONLY);
                            }
                            player.sendSystemMessage(Component.literal("✔ Received Custom Crafting Core T3!").withStyle(ChatFormatting.GOLD), false);
                            return 1;
                        })));

        // /invsee command - view player inventories (admin only)
        dispatcher.register(Commands.literal("invsee")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS)) // Admin only
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .executes(ctx -> {
                            ServerPlayer admin = ctx.getSource().getPlayerOrException();
                            var profiles = GameProfileArgument.getGameProfiles(ctx, "player");
                            if (profiles.isEmpty()) {
                                admin.sendSystemMessage(Component.literal("Player not found!").withStyle(ChatFormatting.RED), false);
                                return 0;
                            }
                            var entry = profiles.iterator().next();
                            java.util.UUID uuid = entry.id();
                            String name = entry.name();
                            ServerPlayer target = PoliticalServer.server.getPlayerList().getPlayer(uuid);
                            if (target != null) {
                                AdminInvseeGui.openMainMenu(admin, target);
                            } else {
                                // Offline player - open limited view with uuid and name
                                AdminInvseeGui.openOfflineMainMenu(admin, uuid, name);
                            }
                            return 1;
                        })
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    return builder.suggest("menu")
                                            .suggest("inventory")
                                            .suggest("ender")
                                            .suggest("bank")
                                            .suggest("coins")
                                            .suggest("wallet")
                                            .suggest("crypto")
                                            .suggest("stock")
                                            .suggest("stocks")
                                            .suggest("prevchests")
                                            .buildFuture();
                                })
                                .executes(ctx -> {
                                    ServerPlayer admin = ctx.getSource().getPlayerOrException();
                                    var profiles = GameProfileArgument.getGameProfiles(ctx, "player");
                                    if (profiles.isEmpty()) {
                                        admin.sendSystemMessage(Component.literal("Player not found!").withStyle(ChatFormatting.RED), false);
                                        return 0;
                                    }
                                    var entry = profiles.iterator().next();
                                    java.util.UUID uuid = entry.id();
                                    String name = entry.name();
                                    ServerPlayer target = PoliticalServer.server.getPlayerList().getPlayer(uuid);
                                    String type = StringArgumentType.getString(ctx, "type");
                                    if (target != null) {
                                        InvseeGui.open(admin, target, type);
                                    } else {
                                        // Offline player - open limited view
                                        InvseeGui.openOffline(admin, uuid, name, type);
                                    }
                                    return 1;
                                }))));

        // /setcoins command - set player coins (admin only)
        dispatcher.register(Commands.literal("setcoins")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS)) // Admin only
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                .executes(ctx -> {
                                    ServerPlayer admin = ctx.getSource().getPlayerOrException();
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                    CoinManager.setCoins(target.getUUID().toString(), amount);
                                    admin.sendSystemMessage(Component.literal("✓ Set " + target.getName().getString() + "'s coins to " + amount)
                                        .withStyle(ChatFormatting.GREEN), false);
                                    return 1;
                                }))));

        // /bountyconfig command - configure bounty settings (admin only)
        dispatcher.register(Commands.literal("bountyconfig")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS)) // Admin only
                .executes(ctx -> {
                    ServerPlayer admin = ctx.getSource().getPlayerOrException();
                    BountyConfigGui.open(admin);
                    return 1;
                }));

    }

    private static void registerCivilAntiCheatAndViewCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Shop item management GUI
        dispatcher.register(Commands.literal("shopitem")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(ctx -> {
                    ShopItemGui.open(ctx.getSource().getPlayerOrException());
                    return 1;
                }));
        
        dispatcher.register(Commands.literal("CivilAC")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(ctx -> {
                    CivilAntiCheatGui.openFrom(ctx.getSource().getPlayerOrException(), null);
                    return 1;
                }));
        dispatcher.register(Commands.literal("civilac")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(ctx -> {
                    CivilAntiCheatGui.openFrom(ctx.getSource().getPlayerOrException(), null);
                    return 1;
                }));
        dispatcher.register(Commands.literal("CivilAntiCheat")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(ctx -> {
                    CivilAntiCheatGui.openFrom(ctx.getSource().getPlayerOrException(), null);
                    return 1;
                }));
        dispatcher.register(Commands.literal("civilanticheat")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(ctx -> {
                    CivilAntiCheatGui.openFrom(ctx.getSource().getPlayerOrException(), null);
                    return 1;
                }));
        // /view accepts both "/view <player> prevchests" and "/view prevchests <player>". They were two
        // registrations of the same root; they are one now (M1.8), with the same two forms.
        dispatcher.register(Commands.literal("view")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.literal("prevchests")
                                .executes(ctx -> {
                                    ServerPlayer adminP = ctx.getSource().getPlayerOrException();
                                    ServerPlayer watched = EntityArgument.getPlayer(ctx, "player");
                                    PrevChestsAdminGui.open(adminP, watched);
                                    return 1;
                                })))
                .then(Commands.literal("prevchests")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer adminP = ctx.getSource().getPlayerOrException();
                                    ServerPlayer watched = EntityArgument.getPlayer(ctx, "player");
                                    PrevChestsAdminGui.open(adminP, watched);
                                    return 1;
                                }))));
        dispatcher.register(Commands.literal("View")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.literal("prevchests")
                                .executes(ctx -> {
                                    ServerPlayer adminP = ctx.getSource().getPlayerOrException();
                                    ServerPlayer watched = EntityArgument.getPlayer(ctx, "player");
                                    PrevChestsAdminGui.open(adminP, watched);
                                    return 1;
                                }))));
        dispatcher.register(Commands.literal("quarantine")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer adminP = ctx.getSource().getPlayerOrException();
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                            DupeGuard.captureForQuarantine(target);
                            int n = DupeGuard.quarantinePlayer(target);
                            adminP.sendSystemMessage(Component.literal(
                                    "Quarantine removed " + n + " stack(s) from " + target.getName().getString())
                                    .withStyle(ChatFormatting.GREEN), false);
                            return 1;
                        })));
    }

    private static void registerResetImpeachment(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("resetimpeachment")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;

                    if (!PoliticalServer.isOperator(player)) {
                        player.sendSystemMessage(Component.literal("You don't have permission to use this command!").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    ElectionManager.resetImpeachment();
                    player.sendSystemMessage(Component.literal("Impeachment has been reset!").withStyle(ChatFormatting.GREEN));
                    return 1;
                }));
    }

    private static void registerPardon(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("Govpardon")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer source = context.getSource().getPlayer();
                            if (source == null) return 0;

                            String sourceUuid = source.getUUID().toString();

                            if (!DictatorManager.hasJudgePermissions(sourceUuid) && !PoliticalServer.isOperator(source)) {
                                source.sendSystemMessage(Component.literal("Only the Judge can use this command!").withStyle(ChatFormatting.RED));
                                return 0;
                            }

                            ServerPlayer target = EntityArgument.getPlayer(context, "player");

                            PrisonManager.release(target);

                            for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
                                p.sendSystemMessage(Component.literal("⚖ " + target.getName().getString() + " has been pardoned by the Judge!").withStyle(ChatFormatting.GREEN));
                            }

                            target.sendSystemMessage(Component.literal("You have been pardoned!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                            return 1;
                        })));
    }

    // ============================================================
    // SHOP PRICE COMMANDS (OP Level 4)
    // ============================================================
    private static void registerShopPriceCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /setprice (no args) → opens GUI
        dispatcher.register(Commands.literal("setprice")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayer();
                    if (player == null) return 0;
                    SetPriceGui.openMainMenu(player);
                    return 1;
                })
                .then(Commands.argument("item", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .then(Commands.argument("sellPrice", IntegerArgumentType.integer(0))
                                .executes(ctx -> {
                                    String itemId = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "item");
                                    int price = IntegerArgumentType.getInteger(ctx, "sellPrice");
                                    Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.tryParse(itemId));
                                    if (item == null || item == Items.AIR) {
                                        ctx.getSource().sendSuccess(() ->
                                                Component.literal("Unknown item: " + itemId).withStyle(ChatFormatting.RED), false);
                                        return 0;
                                    }
                                    ShopManager.setCustomPrice(item, price);
                                    ctx.getSource().sendSuccess(() ->
                                            Component.literal("✓ Set sell price of " + itemId + " to " + price + " coins (buy: " + price * 3 + ")")
                                                    .withStyle(ChatFormatting.GREEN), true);
                                    return 1;
                                })
                        )
                )
        );

        // /getprice <item_id>
        dispatcher.register(Commands.literal("getprice")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .then(Commands.argument("item", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .executes(ctx -> {
                            String itemId = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "item");
                            Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.tryParse(itemId));
                            if (item == null || item == Items.AIR) {
                                ctx.getSource().sendSuccess(() ->
                                        Component.literal("Unknown item: " + itemId).withStyle(ChatFormatting.RED), false);
                                return 0;
                            }
                            int sell = ShopManager.getSellPrice(item);
                            int buy = ShopManager.getBuyPrice(item);
                            boolean disabled = ShopManager.isDisabled(item);
                            ctx.getSource().sendSuccess(() ->
                                    Component.literal("Price of " + itemId + ": sell=" + sell + " buy=" + buy +
                                            (disabled ? " [DISABLED]" : "")).withStyle(ChatFormatting.YELLOW), false);
                            return 1;
                        })
                )
        );

        // /disableshopitem <item_id>
        dispatcher.register(Commands.literal("disableshopitem")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .then(Commands.argument("item", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .executes(ctx -> {
                            String itemId = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "item");
                            Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.tryParse(itemId));
                            if (item == null || item == Items.AIR) {
                                ctx.getSource().sendSuccess(() ->
                                        Component.literal("Unknown item: " + itemId).withStyle(ChatFormatting.RED), false);
                                return 0;
                            }
                            ShopManager.disableItem(item);
                            ctx.getSource().sendSuccess(() ->
                                    Component.literal("✓ Disabled " + itemId + " from shop.").withStyle(ChatFormatting.GREEN), true);
                            return 1;
                        })
                )
        );

        // /enableshopitem <item_id>
        dispatcher.register(Commands.literal("enableshopitem")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .then(Commands.argument("item", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .executes(ctx -> {
                            String itemId = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "item");
                            Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.tryParse(itemId));
                            if (item == null || item == Items.AIR) {
                                ctx.getSource().sendSuccess(() ->
                                        Component.literal("Unknown item: " + itemId).withStyle(ChatFormatting.RED), false);
                                return 0;
                            }
                            ShopManager.enableItem(item);
                            ctx.getSource().sendSuccess(() ->
                                    Component.literal("✓ Re-enabled " + itemId + " in shop.").withStyle(ChatFormatting.GREEN), true);
                            return 1;
                        })
                )
        );

        // /resetprices
        dispatcher.register(Commands.literal("resetprices")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(ctx -> {
                    ShopManager.resetPrices();
                    ctx.getSource().sendSuccess(() ->
                            Component.literal("✓ All shop prices reset to defaults.").withStyle(ChatFormatting.GREEN), true);
                    return 1;
                })
        );

        // /listprices [category]
        dispatcher.register(Commands.literal("listprices")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(ctx -> {
                    // List all categories
                    List<String> cats = ShopManager.getCategoryNames();
                    ctx.getSource().sendSuccess(() ->
                            Component.literal("Shop categories: " + String.join(", ", cats)).withStyle(ChatFormatting.YELLOW), false);
                    // List custom overrides
                    Map<Item, Integer> overrides = ShopManager.getCustomOverrides();
                    if (!overrides.isEmpty()) {
                        ctx.getSource().sendSuccess(() ->
                                Component.literal("Custom price overrides:").withStyle(ChatFormatting.GOLD), false);
                        overrides.forEach((it, price) -> {
                            String id = ItemRegistryIds.asString(it);
                            ctx.getSource().sendSuccess(() ->
                                    Component.literal("  " + id + ": sell=" + price).withStyle(ChatFormatting.WHITE), false);
                        });
                    }
                    Set<Item> disabledSet = ShopManager.getDisabledItems();
                    if (!disabledSet.isEmpty()) {
                        ctx.getSource().sendSuccess(() ->
                                Component.literal("Disabled items:").withStyle(ChatFormatting.RED), false);
                        disabledSet.forEach(it -> {
                            String id = ItemRegistryIds.asString(it);
                            ctx.getSource().sendSuccess(() ->
                                    Component.literal("  " + id).withStyle(ChatFormatting.DARK_RED), false);
                        });
                    }
                    return 1;
                })
                .then(Commands.argument("category", com.mojang.brigadier.arguments.StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String cat = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "category");
                            List<Item> catItems = ShopManager.getItemsInCategory(cat);
                            if (catItems.isEmpty()) {
                                ctx.getSource().sendSuccess(() ->
                                        Component.literal("No items found in category: " + cat).withStyle(ChatFormatting.RED), false);
                                return 0;
                            }
                            ctx.getSource().sendSuccess(() ->
                                    Component.literal("Items in " + cat + ":").withStyle(ChatFormatting.GOLD), false);
                            for (Item it : catItems) {
                                String id = ItemRegistryIds.asString(it);
                                int sell = ShopManager.getSellPrice(it);
                                int buy = ShopManager.getBuyPrice(it);
                                boolean dis = ShopManager.isDisabled(it);
                                ctx.getSource().sendSuccess(() ->
                                        Component.literal("  " + id + ": sell=" + sell + " buy=" + buy +
                                                (dis ? " [DISABLED]" : "")).withStyle(ChatFormatting.WHITE), false);
                            }
                            return 1;
                        })
                )
        );
    }

    // ============================================================
    // SOFT VANISH COMMAND (OP Level 2)
    // ============================================================
    private static void registerVanish(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("softvanish")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    VanishManager.toggleVanish(player);
                    return 1;
                })
        );
    }

    // Custom mob commands removed - no longer needed

    private static void registerModHelp(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("modhelp")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    String uuid = player.getUUID().toString();

                    player.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.GOLD));
                    player.sendSystemMessage(Component.literal("        ⚔ CIVILCRAFT COMMANDS ⚔").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
                    player.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.GOLD));
                    player.sendSystemMessage(Component.literal("").withStyle(ChatFormatting.GRAY));
                    player.sendSystemMessage(Component.literal("/cc").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                            .append(Component.literal(" — Clickable command menu (use this first!)").withStyle(ChatFormatting.YELLOW)));
                    player.sendSystemMessage(Component.literal("").withStyle(ChatFormatting.GRAY));
                    player.sendSystemMessage(Component.literal("  § Economy").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
                    player.sendSystemMessage(Component.literal("/shop").withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(" — Buy and sell items, convert credits→coins").withStyle(ChatFormatting.GRAY)));
                    player.sendSystemMessage(Component.literal("/bank").withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(" — Deposits, savings accounts & loans").withStyle(ChatFormatting.GRAY)));
                    player.sendSystemMessage(Component.literal("/credits").withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(" — Check your credit balance").withStyle(ChatFormatting.GRAY)));
                    player.sendSystemMessage(Component.literal("").withStyle(ChatFormatting.GRAY));
                    player.sendSystemMessage(Component.literal("  ⚔ Bounty Hunting").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                    player.sendSystemMessage(Component.literal("/bounties").withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(" — View bounty board & start quests").withStyle(ChatFormatting.GRAY)));
                    player.sendSystemMessage(Component.literal("/customitems").withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(" — View all custom items & recipes").withStyle(ChatFormatting.GRAY)));
                    player.sendSystemMessage(Component.literal("/recipe").withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(" — Recipe browser (same categories)").withStyle(ChatFormatting.GRAY)));
                    player.sendSystemMessage(Component.literal("/class").withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(" — Classes (buffs & progression)").withStyle(ChatFormatting.GRAY)));
                    player.sendSystemMessage(Component.literal("/skills").withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(" — Skills hub (trees, class shortcut)").withStyle(ChatFormatting.GRAY)));
                    player.sendSystemMessage(Component.literal("/crypto").withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(" — Crypto market").withStyle(ChatFormatting.GRAY)));
                    player.sendSystemMessage(Component.literal("/stocks").withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(" — Stock market").withStyle(ChatFormatting.GRAY)));
                    player.sendSystemMessage(Component.literal("").withStyle(ChatFormatting.GRAY));
                    player.sendSystemMessage(Component.literal("  ⚖ Government").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
                    player.sendSystemMessage(Component.literal("/vote").withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(" — Vote in elections").withStyle(ChatFormatting.GRAY)));
                    player.sendSystemMessage(Component.literal("/intercom").withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(" — Broadcast a server-wide intercom message").withStyle(ChatFormatting.GRAY)));
                    player.sendSystemMessage(Component.literal("").withStyle(ChatFormatting.GRAY));
                    player.sendSystemMessage(Component.literal("  § Other").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
                    player.sendSystemMessage(Component.literal("/modhelp").withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(" — Show this help message").withStyle(ChatFormatting.GRAY)));
                    player.sendSystemMessage(Component.literal("/pbuff").withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(" — View your active player buffs").withStyle(ChatFormatting.GRAY)));
                    player.sendSystemMessage(Component.literal("/checkpoint").withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(" — Manage your personal checkpoints").withStyle(ChatFormatting.GRAY)));

                    // Leader-only commands
                    boolean isChair = uuid.equals(DataManager.getChair());
                    boolean isVice = uuid.equals(DataManager.getViceChair());
                    boolean isDictator = DictatorManager.isDictator(uuid);

                    if (isChair || isVice || isDictator) {
                        player.sendSystemMessage(Component.literal("").withStyle(ChatFormatting.GRAY));
                        player.sendSystemMessage(Component.literal("  👑 Leadership Commands").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                    }
                    if (isChair) {
                        player.sendSystemMessage(Component.literal("/chair").withStyle(ChatFormatting.AQUA)
                                .append(Component.literal(" — Chair government panel").withStyle(ChatFormatting.GRAY)));
                    }
                    if (isVice) {
                        player.sendSystemMessage(Component.literal("/vicechair").withStyle(ChatFormatting.AQUA)
                                .append(Component.literal(" — Vice Chair government panel").withStyle(ChatFormatting.GRAY)));
                    }
                    if (isDictator) {
                        player.sendSystemMessage(Component.literal("/dictator").withStyle(ChatFormatting.RED)
                                .append(Component.literal(" — Dictator control panel").withStyle(ChatFormatting.GRAY)));
                    }

                    player.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.GOLD));
                    return 1;
                }));
    }

    // ── /sethelp ─────────────────────────────────────────────────────
    private static void registerSetHelp(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sethelp")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    openSetHelpGui(player);
                    return 1;
                }));
    }

    private static void openSetHelpGui(ServerPlayer player) {
        // Create a book GUI for editing help content
        eu.pb4.sgui.api.gui.SimpleGui gui = new eu.pb4.sgui.api.gui.SimpleGui(
            net.minecraft.world.inventory.MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("📖 Edit /modhelp"));

        // Background
        eu.pb4.sgui.api.elements.GuiElementBuilder bg = new eu.pb4.sgui.api.elements.GuiElementBuilder(
            net.minecraft.world.item.Items.STAINED_GLASS_PANE.gray()).setName(Component.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        // Header
        gui.setSlot(4, new eu.pb4.sgui.api.elements.GuiElementBuilder(net.minecraft.world.item.Items.BOOK)
                .setName(Component.literal("📖 Edit Help Message").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click sections below to edit").withStyle(ChatFormatting.GRAY))
                .glow().build());

        // Editable sections
        gui.setSlot(10, new eu.pb4.sgui.api.elements.GuiElementBuilder(net.minecraft.world.item.Items.WRITABLE_BOOK)
                .setName(Component.literal("✏ Edit Economy Section").withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to edit economy commands").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, type, containerInput, slotGui) -> {
                    player.sendSystemMessage(Component.literal("Feature coming soon!").withStyle(ChatFormatting.YELLOW));
                }).build());

        gui.setSlot(12, new eu.pb4.sgui.api.elements.GuiElementBuilder(net.minecraft.world.item.Items.WRITABLE_BOOK)
                .setName(Component.literal("✏ Edit Bounty Section").withStyle(ChatFormatting.RED))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to edit bounty commands").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, type, containerInput, slotGui) -> {
                    player.sendSystemMessage(Component.literal("Feature coming soon!").withStyle(ChatFormatting.YELLOW));
                }).build());

        gui.setSlot(14, new eu.pb4.sgui.api.elements.GuiElementBuilder(net.minecraft.world.item.Items.WRITABLE_BOOK)
                .setName(Component.literal("✏ Edit Government Section").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to edit government commands").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, type, containerInput, slotGui) -> {
                    player.sendSystemMessage(Component.literal("Feature coming soon!").withStyle(ChatFormatting.YELLOW));
                }).build());

        gui.setSlot(16, new eu.pb4.sgui.api.elements.GuiElementBuilder(net.minecraft.world.item.Items.WRITABLE_BOOK)
                .setName(Component.literal("✏ Edit Other Section").withStyle(ChatFormatting.LIGHT_PURPLE))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to edit other commands").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, type, containerInput, slotGui) -> {
                    player.sendSystemMessage(Component.literal("Feature coming soon!").withStyle(ChatFormatting.YELLOW));
                }).build());

        gui.setSlot(31, new eu.pb4.sgui.api.elements.GuiElementBuilder(net.minecraft.world.item.Items.WRITTEN_BOOK)
                .setName(Component.literal("👁 Preview Current Help").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to preview /modhelp").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, type, containerInput, slotGui) -> {
                    gui.close();
                    PoliticalServer.server.execute(() -> {
                        try {
                            PoliticalServer.server.getCommands().getDispatcher().execute("modhelp", player.createCommandSourceStack());
                        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
                            player.sendSystemMessage(Component.literal("Error previewing help").withStyle(ChatFormatting.RED));
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
    private static void registerSpawnProtection(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spawnprotection")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))

                // /spawnprotection clear
                .then(Commands.literal("clear")
                        .executes(context -> {
                            SpawnProtectionManager.clearRegion();
                            context.getSource().sendSuccess(() ->
                                    Component.literal("✓ Spawn protection cleared.").withStyle(ChatFormatting.GREEN), true);
                            return 1;
                        })
                )

                // /spawnprotection info
                .then(Commands.literal("info")
                        .executes(context -> {
                            CommandSourceStack src = context.getSource();
                            if (!SpawnProtectionManager.isActive()) {
                                src.sendSuccess(() -> Component.literal("Spawn protection is currently inactive.")
                                        .withStyle(ChatFormatting.YELLOW), false);
                            } else {
                                src.sendSuccess(() -> Component.literal("§aSpawn protection is ACTIVE§r:").withStyle(ChatFormatting.GREEN), false);
                                src.sendSuccess(() -> Component.literal(String.format(
                                        "  X: %d to %d  |  Y: %d to %d  |  Z: %d to %d",
                                        SpawnProtectionManager.getMinX(), SpawnProtectionManager.getMaxX(),
                                        SpawnProtectionManager.getMinY(), SpawnProtectionManager.getMaxY(),
                                        SpawnProtectionManager.getMinZ(), SpawnProtectionManager.getMaxZ()
                                )).withStyle(ChatFormatting.AQUA), false);
                            }
                            return 1;
                        })
                )

                // /spawnprotection <x1> <y1> <z1> <x2> <y2> <z2> [true]
                .then(Commands.argument("x1", IntegerArgumentType.integer())
                        .then(Commands.argument("y1", IntegerArgumentType.integer())
                                .then(Commands.argument("z1", IntegerArgumentType.integer())
                                        .then(Commands.argument("x2", IntegerArgumentType.integer())
                                                .then(Commands.argument("y2", IntegerArgumentType.integer())
                                                        .then(Commands.argument("z2", IntegerArgumentType.integer())
                                                                // without 'true' — use given Y values
                                                                .executes(context -> {
                                                                    int x1 = IntegerArgumentType.getInteger(context, "x1");
                                                                    int y1 = IntegerArgumentType.getInteger(context, "y1");
                                                                    int z1 = IntegerArgumentType.getInteger(context, "z1");
                                                                    int x2 = IntegerArgumentType.getInteger(context, "x2");
                                                                    int y2 = IntegerArgumentType.getInteger(context, "y2");
                                                                    int z2 = IntegerArgumentType.getInteger(context, "z2");
                                                                    SpawnProtectionManager.setRegion(x1, y1, z1, x2, y2, z2);
                                                                    context.getSource().sendSuccess(() -> Component.literal(
                                                                            String.format("✓ Spawn protection set: (%d,%d,%d) → (%d,%d,%d)",
                                                                                    x1, y1, z1, x2, y2, z2)
                                                                    ).withStyle(ChatFormatting.GREEN), true);
                                                                    return 1;
                                                                })
                                                                // with 'true' — span full world height
                                                                .then(Commands.literal("true")
                                                                        .executes(context -> {
                                                                            int x1 = IntegerArgumentType.getInteger(context, "x1");
                                                                            int z1 = IntegerArgumentType.getInteger(context, "z1");
                                                                            int x2 = IntegerArgumentType.getInteger(context, "x2");
                                                                            int z2 = IntegerArgumentType.getInteger(context, "z2");
                                                                            ServerLevel world = context.getSource().getLevel();
                                                                            int yMin = world.getMinY();
                                                                            int yMax = world.getMaxY();
                                                                            SpawnProtectionManager.setRegion(x1, yMin, z1, x2, yMax, z2);
                                                                            context.getSource().sendSuccess(() -> Component.literal(
                                                                                    String.format("✓ Spawn protection set (full height): (%d,%d) → (%d,%d) Y: %d to %d",
                                                                                            x1, z1, x2, z2, yMin, yMax)
                                                                            ).withStyle(ChatFormatting.GREEN), true);
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

    private static void registerPlayerBuff(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("playerbuff")
                // Info subcommand - available to all players
                .then(Commands.literal("info")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            PlayerBuffGui.openMainMenu(player);
                            return 1;
                        }))
                // Admin commands - require owner permission
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("buff", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    for (PlayerBuffManager.PlayerBuff buff : PlayerBuffManager.PlayerBuff.values()) {
                                        builder.suggest(buff.id);
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.literal("add")
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            String buffId = StringArgumentType.getString(context, "buff");
                                            PlayerBuffManager.PlayerBuff buff = PlayerBuffManager.PlayerBuff.fromId(buffId);
                                            if (buff == null) {
                                                context.getSource().sendSuccess(() -> 
                                                        Component.literal("✗ Invalid buff: " + buffId).withStyle(ChatFormatting.RED), false);
                                                return 0;
                                            }
                                            if (PlayerBuffManager.hasBuff(target.getUUID().toString(), buff)) {
                                                context.getSource().sendSuccess(() -> 
                                                        Component.literal("✗ Player already has this buff.").withStyle(ChatFormatting.YELLOW), false);
                                                return 0;
                                            }
                                            PlayerBuffManager.grantBuff(target, buff);
                                            context.getSource().sendSuccess(() -> 
                                                    Component.literal("✓ Added " + buff.displayName + " to " + target.getName().getString())
                                                            .withStyle(ChatFormatting.GREEN), true);
                                            return 1;
                                        }))
                                .then(Commands.literal("remove")
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            String buffId = StringArgumentType.getString(context, "buff");
                                            if (PlayerBuffManager.removeBuff(target.getUUID().toString(), buffId)) {
                                                DataManager.save(PoliticalServer.server);
                                                context.getSource().sendSuccess(() -> 
                                                        Component.literal("✓ Removed buff from " + target.getName().getString())
                                                                .withStyle(ChatFormatting.GREEN), true);
                                                return 1;
                                            } else {
                                                context.getSource().sendSuccess(() -> 
                                                        Component.literal("✗ Player doesn't have that buff.").withStyle(ChatFormatting.YELLOW), false);
                                                return 0;
                                            }
                                        })))
                        .then(Commands.literal("list")
                                .executes(context -> {
                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                    Set<PlayerBuffManager.PlayerBuff> buffs = PlayerBuffManager.getBuffObjects(target.getUUID().toString());
                                    if (buffs.isEmpty()) {
                                        context.getSource().sendSuccess(() -> 
                                                Component.literal(target.getName().getString() + " has no buffs.").withStyle(ChatFormatting.YELLOW), false);
                                    } else {
                                        context.getSource().sendSuccess(() -> 
                                                Component.literal("§aBuffs for " + target.getName().getString() + ":"), false);
                                        for (PlayerBuffManager.PlayerBuff buff : buffs) {
                                            context.getSource().sendSuccess(() -> 
                                                    Component.literal("  " + buff.colorCode + "◆ " + buff.displayName + "§7 - " + buff.effect), false);
                                        }
                                    }
                                    return 1;
                                })))
                .then(Commands.literal("listall")
                        .executes(context -> {
                            context.getSource().sendSuccess(() -> 
                                    Component.literal("§aAll available buffs:"), false);
                            for (PlayerBuffManager.PlayerBuff buff : PlayerBuffManager.PlayerBuff.values()) {
                                context.getSource().sendSuccess(() -> 
                                        Component.literal("  " + buff.colorCode + "◆ " + buff.displayName + 
                                                "§7 - " + buff.obtainMethod), false);
                            }
                            return 1;
                        })));
    }

    // ============================================================
    // /PBUFF COMMAND - Shows all buffs (unlocked + locked)
    // ============================================================

    private static void registerPBuff(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pbuff")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    PlayerBuffGui.openMainMenu(player);
                    return 1;
                }));
    }

    // ============================================================
    // /PLAYERBUFFADMIN COMMAND - GUI for managing player buffs
    // ============================================================

    private static void registerPlayerBuffAdmin(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("playerbuffadmin")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    PlayerBuffAdminGui.openPlayerSelectFrom(player, null);
                    return 1;
                }));
    }

    // ============================================================
    // PAY COMMAND
    // ============================================================

    private static void registerPay(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pay")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                .executes(context -> {
                                    ServerPlayer sender = context.getSource().getPlayerOrException();
                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                    long amount = LongArgumentType.getLong(context, "amount");

                                    if (sender.getUUID().equals(target.getUUID())) {
                                        sender.sendSystemMessage(Component.literal("You cannot pay yourself.")
                                                .withStyle(ChatFormatting.RED), false);
                                        return 0;
                                    }

                                    if (!CoinManager.transferExact(sender, target, amount)) {
                                        sender.sendSystemMessage(Component.literal("Not enough coins. You have " + CoinManager.getCoinsLong(sender))
                                                .withStyle(ChatFormatting.RED), false);
                                        return 0;
                                    }

                                    sender.sendSystemMessage(Component.literal("Sent " + amount + " coins to " + target.getName().getString())
                                            .withStyle(ChatFormatting.GREEN), false);
                                    target.sendSystemMessage(Component.literal("Received " + amount + " coins from " + sender.getName().getString())
                                            .withStyle(ChatFormatting.GREEN), false);

                                    DataManager.save(PoliticalServer.server);
                                    return 1;
                                }))));
    }

    // ============================================================
    // PARTY COMMANDS (for MENTOR buff)
    // ============================================================

    private static void registerParty(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("playerparty")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    PartyManager.listParty(player);
                    return 1;
                })
                .then(Commands.literal("create")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            PartyManager.createParty(player);
                            return 1;
                        }))
                .then(Commands.literal("invite")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer leader = context.getSource().getPlayerOrException();
                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                    PartyManager.invitePlayer(leader, target);
                                    return 1;
                                })))
                .then(Commands.literal("accept")
                        .then(Commands.argument("leader", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ServerPlayer leader = EntityArgument.getPlayer(context, "leader");
                                    PartyManager.acceptInvite(player, leader);
                                    return 1;
                                })))
                .then(Commands.literal("leave")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            PartyManager.leaveParty(player);
                            return 1;
                        }))
                .then(Commands.literal("list")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            PartyManager.listParty(player);
                            return 1;
                        })));
    }

    /**
     * /checkpoint command - Opens checkpoint GUI for teleportation, creation, deletion, and renaming.
     */
    private static void registerCheckpoint(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("checkpoint")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CheckpointGui.openMain(player);
                    return 1;
                })
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    String name = StringArgumentType.getString(context, "name");
                                    CheckpointManager.createCheckpoint(player, name);
                                    return 1;
                                })))
                .then(Commands.literal("delete")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    String id = StringArgumentType.getString(context, "id");
                                    CheckpointManager.deleteCheckpoint(player, id);
                                    return 1;
                                })))
                .then(Commands.literal("rename")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            String id = StringArgumentType.getString(context, "id");
                                            String name = StringArgumentType.getString(context, "name");
                                            CheckpointManager.renameCheckpoint(player, id, name);
                                            return 1;
                                        }))))
                .then(Commands.literal("tp")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    String id = StringArgumentType.getString(context, "id");
                                    CheckpointManager.teleportToCheckpoint(player, id);
                                    return 1;
                                })))
                .then(Commands.literal("list")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            java.util.List<CheckpointManager.Checkpoint> checkpoints = 
                                CheckpointManager.getPlayerCheckpoints(player.getUUID().toString());
                            if (checkpoints.isEmpty()) {
                                player.sendSystemMessage(Component.literal("You have no checkpoints.")
                                        .withStyle(ChatFormatting.GRAY));
                            } else {
                                player.sendSystemMessage(Component.literal("Your checkpoints:")
                                        .withStyle(ChatFormatting.GOLD));
                                for (CheckpointManager.Checkpoint cp : checkpoints) {
                                    player.sendSystemMessage(Component.literal(" - [" + cp.id + "] " + cp.name)
                                            .withStyle(ChatFormatting.YELLOW));
                                }
                            }
                            return 1;
                        })));
    }

    /**
     * Exports all texture codes in a simple text format for easy copying.
     */
    private static void exportTextureCodesTxt(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("=== TEXTURE PACK CODES ===").withStyle(ChatFormatting.GOLD), false);
        player.sendSystemMessage(Component.literal(""), false);
        
        java.util.Map<String, java.util.List<TexturePackCodes.CustomItemEntry>> categories = 
            TexturePackCodes.getAllItemsByCategory();
        
        for (java.util.Map.Entry<String, java.util.List<TexturePackCodes.CustomItemEntry>> entry : categories.entrySet()) {
            player.sendSystemMessage(Component.literal("[" + entry.getKey() + "]").withStyle(ChatFormatting.AQUA), false);
            
            for (TexturePackCodes.CustomItemEntry item : entry.getValue()) {
                player.sendSystemMessage(Component.literal(item.id() + " = " + item.textureCode()), false);
            }
            player.sendSystemMessage(Component.literal(""), false);
        }
        
        player.sendSystemMessage(Component.literal("=== END (" + TexturePackCodes.getAllItems().size() + " items) ===").withStyle(ChatFormatting.GOLD), false);
        player.sendSystemMessage(Component.literal("Use /texturehelper for GUI view").withStyle(ChatFormatting.GRAY), false);
    }

    // ── /civilcraft and /cc ─────────────────────────────────────────────────────
    private static void registerCivilCraft(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Main command
        dispatcher.register(Commands.literal("civilcraft")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    CivilCraftGui.open(player);
                    return 1;
                }));

        // Short alias
        dispatcher.register(Commands.literal("cc")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    CivilCraftGui.open(player);
                    return 1;
                }));
    }

    // ── /cspawn ─────────────────────────────────────────────────────────────────────
    private static void registerCustomSpawn(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cspawn")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .then(Commands.argument("type", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            builder.suggest("elite");
                            builder.suggest("champion");
                            builder.suggest("enchanted");
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            String type = StringArgumentType.getString(context, "type").toLowerCase();
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            
                            net.minecraft.world.entity.EntityType<?> entityType = switch (type) {
                                case "elite" -> net.minecraft.world.entity.EntityTypes.ZOMBIE;
                                case "champion" -> net.minecraft.world.entity.EntityTypes.SKELETON;
                                case "enchanted" -> net.minecraft.world.entity.EntityTypes.VINDICATOR;
                                default -> {
                                    player.sendSystemMessage(Component.literal("Invalid type! Use: elite, champion, or enchanted").withStyle(ChatFormatting.RED));
                                    yield null;
                                }
                            };
                            
                            if (entityType == null) return 1;
                            
                            // Spawn the mob at player's location
                            net.minecraft.world.entity.Entity entity = entityType.create(player.level(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                            if (entity != null) {
                                entity.setPos(player.getX(), player.getY(), player.getZ());
                                // Add custom name
                                entity.setCustomName(Component.literal("§" + (type.equals("elite") ? "c" : type.equals("champion") ? "5" : "9") + "§l" + 
                                    (type.substring(0, 1).toUpperCase() + type.substring(1)) + " " + 
                                    (type.equals("elite") ? "Warrior" : type.equals("champion") ? "Archer" : "Mage")));
                                entity.setCustomNameVisible(true);
                                player.level().addFreshEntity(entity);
                                player.sendSystemMessage(Component.literal("✅ Spawned " + type + " mob!").withStyle(ChatFormatting.GREEN));
                            }
                            return 1;
                        })));
    }

    private static void registerSetTitle(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("settitle")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS)) // Admin only
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("title", StringArgumentType.string())
                                .then(Commands.argument("color", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            // Suggest Minecraft color codes
                                            builder.suggest("0", Component.literal("Black"));
                                            builder.suggest("1", Component.literal("Dark Blue"));
                                            builder.suggest("2", Component.literal("Dark Green"));
                                            builder.suggest("3", Component.literal("Dark Aqua"));
                                            builder.suggest("4", Component.literal("Dark Red"));
                                            builder.suggest("5", Component.literal("Dark Purple"));
                                            builder.suggest("6", Component.literal("Gold"));
                                            builder.suggest("7", Component.literal("Gray"));
                                            builder.suggest("8", Component.literal("Dark Gray"));
                                            builder.suggest("9", Component.literal("Blue"));
                                            builder.suggest("a", Component.literal("Green"));
                                            builder.suggest("b", Component.literal("Aqua"));
                                            builder.suggest("c", Component.literal("Red"));
                                            builder.suggest("d", Component.literal("Light Purple"));
                                            builder.suggest("e", Component.literal("Yellow"));
                                            builder.suggest("f", Component.literal("White"));
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            String title = StringArgumentType.getString(context, "title");
                                            String color = StringArgumentType.getString(context, "color");
                                            
                                            // Validate color code
                                            if (!color.matches("[0-9a-f]")) {
                                                context.getSource().sendFailure(Component.literal("Invalid color code! Use 0-9 or a-f. Available colors: 0=Black, 1=Dark Blue, 2=Dark Green, 3=Dark Aqua, 4=Dark Red, 5=Dark Purple, 6=Gold, 7=Gray, 8=Dark Gray, 9=Blue, a=Green, b=Aqua, c=Red, d=Light Purple, e=Yellow, f=White"));
                                                return 0;
                                            }
                                            
                                            // Store title in player data (using DataManager for persistence)
                                            DataManager.setPlayerTitle(target.getUUID().toString(), title, color);
                                            
                                            // Apply title display
                                            updatePlayerDisplayName(target);
                                            
                                            context.getSource().sendSuccess(() -> 
                                                Component.literal("Set title '" + title + "' with color §" + color + " for " + target.getName().getString())
                                                    .withStyle(ChatFormatting.GREEN), true);
                                            target.sendSystemMessage(Component.literal("You received the title: §" + color + "[" + title + "]")
                                                    .withStyle(ChatFormatting.GREEN), false);
                                            return 1;
                                        })))));
    }

    private static void registerSummonChild(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("summonchild")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS)) // Admin only
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ServerLevel world = (ServerLevel) player.level();
                    
                    // Create baby villager
                    net.minecraft.world.entity.npc.villager.Villager villager = new net.minecraft.world.entity.npc.villager.Villager(
                        net.minecraft.world.entity.EntityTypes.VILLAGER, world);
                    
                    // Set as baby and prevent aging
                    villager.setBaby(true);
                    villager.setPersistenceRequired(); // Prevents despawning
                    
                    // Set custom name
                    villager.setCustomName(Component.literal("victim"));
                    villager.setCustomNameVisible(true);
                    
                    // Position at player location
                    villager.setPos(player.getX(), player.getY(), player.getZ());
                    
                    // Spawn the villager
                    world.addFreshEntity(villager);
                    
                    player.sendSystemMessage(Component.literal("✅ Summoned baby villager 'victim'!")
                        .withStyle(ChatFormatting.GREEN), false);
                    
                    return 1;
                }));
    }

    // Helper method to update player display name with title
    private static void updatePlayerDisplayName(ServerPlayer player) {
        String title = DataManager.getPlayerTitle(player.getUUID().toString());
        if (title != null && !title.isEmpty()) {
            String color = DataManager.getPlayerTitleColor(player.getUUID().toString());
            String displayName = "§" + color + "[" + title + "]§r " + player.getName().getString();
            player.setCustomName(Component.literal(displayName));
            player.setCustomNameVisible(true);
        }
    }

    // ════════════════════════════════════════════════════════════
    // PROGRESSION TREE COMMAND
    // ════════════════════════════════════════════════════════════

    private static void registerTree(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tree")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ProgressionTreeGui.openMainMenu(player);
                    return 1;
                })
                .then(Commands.literal("stats")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ProgressionTreeManager.displayProgress(player);
                            return 1;
                        }))
                .then(Commands.literal("admin")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.literal("addpoints")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(context -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                                    int amount = IntegerArgumentType.getInteger(context, "amount");
                                                    ProgressionTreeManager.awardSkillPoints(target, amount, "admin grant");
                                                    context.getSource().sendSystemMessage(Component.literal("§aGranted " + amount + " skill points to " + target.getName().getString()));
                                                    return 1;
                                                })))
                                .then(Commands.literal("unlocktier")
                                        .then(Commands.argument("branch", StringArgumentType.word())
                                                .executes(context -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                                    String branchName = StringArgumentType.getString(context, "branch").toUpperCase();
                                                    try {
                                                        ProgressionTreeManager.Branch branch = ProgressionTreeManager.Branch.valueOf(branchName);
                                                        if (ProgressionTreeManager.unlockTier(target, branch)) {
                                                            context.getSource().sendSystemMessage(Component.literal("§aUnlocked next tier for " + branch.name + " for " + target.getName().getString()));
                                                        }
                                                    } catch (IllegalArgumentException e) {
                                                        context.getSource().sendSystemMessage(Component.literal("§cInvalid branch. Valid: COMBAT, MINING, FARMING, FISHING, EXPLORATION, CRAFTING"));
                                                    }
                                                    return 1;
                                                }))))));
    }

    // ════════════════════════════════════════════════════════════
    // HUNTS COMMAND
    // ════════════════════════════════════════════════════════════

    /**
     * /hunt. Two registrations of this root used to exist, one for the Great Hunt (LivingWorldCommands) and
     * this one for the older Hunts system. They are one now (M1.8) with the effect Brigadier's merge gave
     * them: the bare "/hunt" opens the older Hunts menu (this registration ran later, so its executor won),
     * and the Great Hunt keeps its sub-commands info, start, toggle and interval. The Great Hunt screen
     * (HuntGui.open) is therefore not reachable from the bare command; whether it should be is a design
     * question for M6 (HANDOFF.md, KNOWN ISSUES).
     */
    private static void registerHunt(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hunt")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    HuntsGui.openMainMenu(player);
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
                                })))
                .then(Commands.literal("claim")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            HuntsManager.checkInventoryForHunt(player);
                            return 1;
                        }))
                .then(Commands.literal("abandon")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            HuntsManager.abandonHunt(player);
                            return 1;
                        }))
                .then(Commands.literal("admin")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.literal("resethunts")
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            String uuid = target.getUUID().toString();
                                            HuntsManager.registerPlayer(uuid);
                                            context.getSource().sendSystemMessage(Component.literal("§aReset hunts for " + target.getName().getString()));
                                            return 1;
                                        }))
                                .then(Commands.literal("completetier")
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            String uuid = target.getUUID().toString();
                                            HuntsManager.HuntTier current = HuntsManager.getPlayerTier(uuid);
                                            if (current.ordinal() < 4) {
                                                // Force tier upgrade logic
                                                context.getSource().sendSystemMessage(Component.literal("§aAdvanced " + target.getName().getString() + " to next hunt tier"));
                                            }
                                            return 1;
                                        })))));
    }

    // ════════════════════════════════════════════════════════════
    // WORLD BOSS COMMAND
    // ════════════════════════════════════════════════════════════

    private static void registerWorldBoss(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("worldboss")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    WorldBossGui.openMainMenu(player);
                    return 1;
                })
                .then(Commands.literal("admin")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            WorldBossGui.openAdminMenu(player);
                            return 1;
                        })
                        .then(Commands.literal("spawn")
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            String typeName = StringArgumentType.getString(context, "type").toUpperCase();
                                            
                                            try {
                                                WorldBossManager.WorldBossType bossType = WorldBossManager.WorldBossType.valueOf(typeName);
                                                ServerLevel world = (ServerLevel) player.level();
                                                WorldBossManager.forceSpawn(world, bossType);
                                                context.getSource().sendSystemMessage(Component.literal("§aSpawned " + bossType.tier.name + " " + bossType.name + "!"));
                                            } catch (IllegalArgumentException e) {
                                                context.getSource().sendSystemMessage(Component.literal("§cInvalid boss type. Valid types: HOLLOW_KNIGHT, MOSSWARDEN, SCORCHFIEND, SKULLTIDE_CAPTAIN, NETHERMAW, WAILING_PHANTOM, CRYSTAL_GOLEM, BLIGHT_MOTHER, VOIDREAVER, INFERNAL_HYDRA, ANCIENT_ONE, KING_OF_ASHES"));
                                            }
                                            return 1;
                                        })))
                        .then(Commands.literal("despawn")
                                .executes(context -> {
                                    WorldBossManager.despawnAll();
                                    context.getSource().sendSystemMessage(Component.literal("§cDespawned all world bosses!"));
                                    return 1;
                        }))));
    }

    private static void registerBosses(CommandDispatcher<CommandSourceStack> dispatcher) {
        final long PRICE_COMMON = 100_000_000L;
        final long PRICE_ELITE = 250_000_000L;
        final long PRICE_LEGENDARY = 500_000_000L;

        dispatcher.register(Commands.literal("bosses")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ServerLevel world = (ServerLevel) player.level();

                    SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
                    gui.setTitle(Component.literal("⚔ World Bosses (12)"));

                    for (int i = 0; i < 54; i++) {
                        gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal("")).build());
                    }

                    gui.setSlot(4, new GuiElementBuilder(Items.BEACON)
                            .setName(Component.literal("⚔ Paid Boss Spawns").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                            .addLoreLine(Component.literal(""))
                            .addLoreLine(Component.literal("Cores + trophies + treasury fragments").withStyle(ChatFormatting.GRAY))
                            .addLoreLine(Component.literal("by contribution. Abilities run automatically.").withStyle(ChatFormatting.GRAY))
                            .glow().build());

                    gui.setSlot(10, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                            .setName(Component.literal("Hollow Knight (Common)").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                            .addLoreLine(Component.literal("Cost: 100M · T1 core + trophy").withStyle(ChatFormatting.YELLOW))
                            .setCallback((s, t, a, __slotGui) -> spawnBossForCoins(context.getSource(), "HOLLOW_KNIGHT", PRICE_COMMON, world, player)).build());
                    gui.setSlot(11, new GuiElementBuilder(Items.IRON_BLOCK)
                            .setName(Component.literal("Mosswarden (Common)").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                            .addLoreLine(Component.literal("Cost: 100M · T1 core + trophy").withStyle(ChatFormatting.YELLOW))
                            .setCallback((s, t, a, __slotGui) -> spawnBossForCoins(context.getSource(), "MOSSWARDEN", PRICE_COMMON, world, player)).build());
                    gui.setSlot(12, new GuiElementBuilder(Items.BLAZE_POWDER)
                            .setName(Component.literal("Scorchfiend (Common)").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                            .addLoreLine(Component.literal("Cost: 100M · T1 core + trophy").withStyle(ChatFormatting.YELLOW))
                            .setCallback((s, t, a, __slotGui) -> spawnBossForCoins(context.getSource(), "SCORCHFIEND", PRICE_COMMON, world, player)).build());
                    gui.setSlot(13, new GuiElementBuilder(Items.TRIDENT)
                            .setName(Component.literal("Skulltide Captain (Common)").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                            .addLoreLine(Component.literal("Cost: 100M · T1 core + trophy").withStyle(ChatFormatting.YELLOW))
                            .setCallback((s, t, a, __slotGui) -> spawnBossForCoins(context.getSource(), "SKULLTIDE_CAPTAIN", PRICE_COMMON, world, player)).build());
                    gui.setSlot(14, new GuiElementBuilder(Items.HOGLIN_SPAWN_EGG)
                            .setName(Component.literal("Nethermaw (Elite)").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                            .addLoreLine(Component.literal("Cost: 250M · T2 core + trophy").withStyle(ChatFormatting.YELLOW))
                            .setCallback((s, t, a, __slotGui) -> spawnBossForCoins(context.getSource(), "NETHERMAW", PRICE_ELITE, world, player)).build());
                    gui.setSlot(15, new GuiElementBuilder(Items.PHANTOM_SPAWN_EGG)
                            .setName(Component.literal("Wailing Phantom (Elite)").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                            .addLoreLine(Component.literal("Cost: 250M · T2 core + trophy").withStyle(ChatFormatting.YELLOW))
                            .setCallback((s, t, a, __slotGui) -> spawnBossForCoins(context.getSource(), "WAILING_PHANTOM", PRICE_ELITE, world, player)).build());

                    gui.setSlot(19, new GuiElementBuilder(Items.DIAMOND_BLOCK)
                            .setName(Component.literal("Crystal Golem (Elite)").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                            .addLoreLine(Component.literal("Cost: 250M · T2 core + trophy").withStyle(ChatFormatting.YELLOW))
                            .setCallback((s, t, a, __slotGui) -> spawnBossForCoins(context.getSource(), "CRYSTAL_GOLEM", PRICE_ELITE, world, player)).build());
                    gui.setSlot(20, new GuiElementBuilder(Items.SPIDER_EYE)
                            .setName(Component.literal("Blight Mother (Elite)").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                            .addLoreLine(Component.literal("Cost: 250M · T2 core + trophy").withStyle(ChatFormatting.YELLOW))
                            .setCallback((s, t, a, __slotGui) -> spawnBossForCoins(context.getSource(), "BLIGHT_MOTHER", PRICE_ELITE, world, player)).build());
                    gui.setSlot(21, new GuiElementBuilder(Items.ENDER_EYE)
                            .setName(Component.literal("Voidreaver (Legendary)").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                            .addLoreLine(Component.literal("Cost: 500M · T3 core + trophy").withStyle(ChatFormatting.YELLOW))
                            .setCallback((s, t, a, __slotGui) -> spawnBossForCoins(context.getSource(), "VOIDREAVER", PRICE_LEGENDARY, world, player)).build());
                    gui.setSlot(22, new GuiElementBuilder(Items.RAVAGER_SPAWN_EGG)
                            .setName(Component.literal("Infernal Hydra (Legendary)").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                            .addLoreLine(Component.literal("Cost: 500M · T3 core + trophy").withStyle(ChatFormatting.YELLOW))
                            .setCallback((s, t, a, __slotGui) -> spawnBossForCoins(context.getSource(), "INFERNAL_HYDRA", PRICE_LEGENDARY, world, player)).build());
                    gui.setSlot(23, new GuiElementBuilder(Items.GUARDIAN_SPAWN_EGG)
                            .setName(Component.literal("Ancient One (Legendary)").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                            .addLoreLine(Component.literal("Cost: 500M · T3 core + trophy").withStyle(ChatFormatting.YELLOW))
                            .setCallback((s, t, a, __slotGui) -> spawnBossForCoins(context.getSource(), "ANCIENT_ONE", PRICE_LEGENDARY, world, player)).build());
                    gui.setSlot(24, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                            .setName(Component.literal("King of Ashes (Legendary)").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                            .addLoreLine(Component.literal("Cost: 500M · T3 core + trophy").withStyle(ChatFormatting.YELLOW))
                            .setCallback((s, t, a, __slotGui) -> spawnBossForCoins(context.getSource(), "KING_OF_ASHES", PRICE_LEGENDARY, world, player)).build());

                    gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                            .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                            .setCallback((s, t, a, __slotGui) -> gui.close()).build());

                    gui.open();
                    return 1;
                })
                .then(Commands.literal("admin")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ServerLevel world = (ServerLevel) player.level();
                            

                            SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
                            gui.setTitle(Component.literal("⚔ Admin — All World Bosses"));

                            for (int i = 0; i < 54; i++) {
                                gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal("")).build());
                            }

                            gui.setSlot(4, new GuiElementBuilder(Items.COMMAND_BLOCK)
                                    .setName(Component.literal("⚔ Admin Boss Spawner").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                                    .addLoreLine(Component.literal("")).addLoreLine(Component.literal("Free boss spawning!").withStyle(ChatFormatting.GREEN)).glow().build());

                            gui.setSlot(10, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL).setName(Component.literal("Hollow Knight").withStyle(ChatFormatting.GREEN))
                                    .setCallback((slot, type, containerInput, slotGui) -> spawnBossAdmin(context.getSource(), "HOLLOW_KNIGHT", world)).build());
                            gui.setSlot(11, new GuiElementBuilder(Items.IRON_BLOCK).setName(Component.literal("Mosswarden").withStyle(ChatFormatting.GREEN))
                                    .setCallback((slot, type, containerInput, slotGui) -> spawnBossAdmin(context.getSource(), "MOSSWARDEN", world)).build());
                            gui.setSlot(12, new GuiElementBuilder(Items.BLAZE_POWDER).setName(Component.literal("Scorchfiend").withStyle(ChatFormatting.GREEN))
                                    .setCallback((slot, type, containerInput, slotGui) -> spawnBossAdmin(context.getSource(), "SCORCHFIEND", world)).build());
                            gui.setSlot(13, new GuiElementBuilder(Items.TRIDENT).setName(Component.literal("Skulltide Captain").withStyle(ChatFormatting.GREEN))
                                    .setCallback((slot, type, containerInput, slotGui) -> spawnBossAdmin(context.getSource(), "SKULLTIDE_CAPTAIN", world)).build());
                            gui.setSlot(14, new GuiElementBuilder(Items.HOGLIN_SPAWN_EGG).setName(Component.literal("Nethermaw").withStyle(ChatFormatting.AQUA))
                                    .setCallback((slot, type, containerInput, slotGui) -> spawnBossAdmin(context.getSource(), "NETHERMAW", world)).build());
                            gui.setSlot(15, new GuiElementBuilder(Items.PHANTOM_SPAWN_EGG).setName(Component.literal("Wailing Phantom").withStyle(ChatFormatting.AQUA))
                                    .setCallback((slot, type, containerInput, slotGui) -> spawnBossAdmin(context.getSource(), "WAILING_PHANTOM", world)).build());
                            gui.setSlot(19, new GuiElementBuilder(Items.DIAMOND_BLOCK).setName(Component.literal("Crystal Golem").withStyle(ChatFormatting.AQUA))
                                    .setCallback((slot, type, containerInput, slotGui) -> spawnBossAdmin(context.getSource(), "CRYSTAL_GOLEM", world)).build());
                            gui.setSlot(20, new GuiElementBuilder(Items.SPIDER_EYE).setName(Component.literal("Blight Mother").withStyle(ChatFormatting.AQUA))
                                    .setCallback((slot, type, containerInput, slotGui) -> spawnBossAdmin(context.getSource(), "BLIGHT_MOTHER", world)).build());
                            gui.setSlot(21, new GuiElementBuilder(Items.ENDER_EYE).setName(Component.literal("Voidreaver").withStyle(ChatFormatting.LIGHT_PURPLE))
                                    .setCallback((slot, type, containerInput, slotGui) -> spawnBossAdmin(context.getSource(), "VOIDREAVER", world)).build());
                            gui.setSlot(22, new GuiElementBuilder(Items.RAVAGER_SPAWN_EGG).setName(Component.literal("Infernal Hydra").withStyle(ChatFormatting.LIGHT_PURPLE))
                                    .setCallback((slot, type, containerInput, slotGui) -> spawnBossAdmin(context.getSource(), "INFERNAL_HYDRA", world)).build());
                            gui.setSlot(23, new GuiElementBuilder(Items.GUARDIAN_SPAWN_EGG).setName(Component.literal("Ancient One").withStyle(ChatFormatting.LIGHT_PURPLE))
                                    .setCallback((slot, type, containerInput, slotGui) -> spawnBossAdmin(context.getSource(), "ANCIENT_ONE", world)).build());
                            gui.setSlot(24, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL).setName(Component.literal("King of Ashes").withStyle(ChatFormatting.LIGHT_PURPLE))
                                    .setCallback((slot, type, containerInput, slotGui) -> spawnBossAdmin(context.getSource(), "KING_OF_ASHES", world)).build());

                            gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                                    .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                                    .setCallback((slot, type, containerInput, slotGui) -> gui.close()).build());

                            gui.open();
                            return 1;
                        })));
    }

    private static void spawnBossForCoins(CommandSourceStack source, String bossTypeName, long cost, ServerLevel world, ServerPlayer player) {
        String uuid = player.getUUID().toString();
        long coins = DataManager.getCoins(uuid);
        
        if (coins < cost) {
            source.sendSystemMessage(Component.literal("§cNot enough coins! Need " + String.format("%,d", cost) + " coins.").withStyle(ChatFormatting.RED));
            return;
        }

        try {
            WorldBossManager.WorldBossType bossType = WorldBossManager.WorldBossType.valueOf(bossTypeName);
            CoinManager.removeCoins(uuid, cost);

            var random = world.getRandom();
            int dx = random.nextInt(21) - 10;
            int dz = random.nextInt(21) - 10;
            BlockPos base = player.blockPosition().offset(dx, 0, dz);
            BlockPos spawnPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, base);

            boolean spawned = WorldBossManager.spawnWorldBossAt(world, bossType, bossType.tier, spawnPos);
            if (spawned) {
                source.sendSystemMessage(Component.literal("§aSpawned " + bossType.name + " for " + String.format("%,d", cost) + " coins! (near you)").withStyle(ChatFormatting.GREEN));
            } else {
                source.sendSystemMessage(Component.literal("§cFailed to spawn boss (invalid location). Your coins were refunded.").withStyle(ChatFormatting.RED));
                CoinManager.giveCoins(player, cost);
            }
        } catch (IllegalArgumentException e) {
            source.sendSystemMessage(Component.literal("§cInvalid boss type!").withStyle(ChatFormatting.RED));
        }
    }

    private static void spawnBossAdmin(CommandSourceStack source, String bossTypeName, ServerLevel world) {
        try {
            WorldBossManager.WorldBossType bossType = WorldBossManager.WorldBossType.valueOf(bossTypeName);

            ServerPlayer player = source.getPlayer();
            BlockPos spawnPos;
            if (player != null) {
                var random = world.getRandom();
                int dx = random.nextInt(21) - 10;
                int dz = random.nextInt(21) - 10;
                BlockPos base = player.blockPosition().offset(dx, 0, dz);
                spawnPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, base);
            } else {
                spawnPos = new BlockPos(0, 64, 0);
            }

            boolean spawned = WorldBossManager.spawnWorldBossAt(world, bossType, bossType.tier, spawnPos);
            if (spawned) {
                source.sendSystemMessage(Component.literal("§a[ADMIN] Spawned " + bossType.name + " (FREE)! (near you)").withStyle(ChatFormatting.GREEN));
            } else {
                source.sendSystemMessage(Component.literal("§c[ADMIN] Failed to spawn boss (invalid location).").withStyle(ChatFormatting.RED));
            }
        } catch (IllegalArgumentException e) {
            source.sendSystemMessage(Component.literal("§cInvalid boss type!").withStyle(ChatFormatting.RED));
        }
    }

    // ════════════════════════════════════════════════════════════
    // WARP COMMAND (Dimension warps for 30M coins)
    // ════════════════════════════════════════════════════════════

    private static void registerWarp(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("warp")
                .then(Commands.literal("nether")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            return warpToDimension(context.getSource(), player, ResourceKey.create(Registries.DIMENSION, Identifier.withDefaultNamespace("the_nether")), "Nether");
                        }))
                .then(Commands.literal("end")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            return warpToDimension(context.getSource(), player, ResourceKey.create(Registries.DIMENSION, Identifier.withDefaultNamespace("the_end")), "The End");
                        }))
                .then(Commands.literal("overworld")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            return warpToDimension(context.getSource(), player, ResourceKey.create(Registries.DIMENSION, Identifier.withDefaultNamespace("overworld")), "Overworld");
                        }))
                .executes(context -> {
                    context.getSource().sendSystemMessage(Component.literal("§6=== Dimension Warps ===").withStyle(ChatFormatting.GOLD));
                    context.getSource().sendSystemMessage(Component.literal("§e/warp nether §7- §f30,000,000 coins").withStyle(ChatFormatting.YELLOW));
                    context.getSource().sendSystemMessage(Component.literal("§e/warp end §7- §f30,000,000 coins").withStyle(ChatFormatting.YELLOW));
                    context.getSource().sendSystemMessage(Component.literal("§e/warp overworld §7- §f30,000,000 coins").withStyle(ChatFormatting.YELLOW));
                    return 1;
                }));
    }

    private static int warpToDimension(CommandSourceStack source, ServerPlayer player, ResourceKey<Level> dimension, String dimName) {
        String uuid = player.getUUID().toString();
        long cost = 30_000_000L;
        long coins = DataManager.getCoins(uuid);

        if (coins < cost) {
            source.sendSystemMessage(Component.literal("§cNot enough coins! Need " + String.format("%,d", cost) + " coins.").withStyle(ChatFormatting.RED));
            return 0;
        }

        MinecraftServer server = source.getServer();
        if (server == null) {
            source.sendSystemMessage(Component.literal("§cServer error.").withStyle(ChatFormatting.RED));
            return 0;
        }

        ServerLevel targetWorld = server.getLevel(dimension);
        if (targetWorld == null) {
            source.sendSystemMessage(Component.literal("§cDimension not found.").withStyle(ChatFormatting.RED));
            return 0;
        }

        CoinManager.removeCoins(uuid, cost);

        BlockPos spawnPos = targetWorld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(0, 64, 0));
        PlayerTeleportHelper.teleport(player, targetWorld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, player.getYRot(), player.getXRot());

        source.sendSystemMessage(Component.literal("§aWarped to " + dimName + " for " + String.format("%,d", cost) + " coins!").withStyle(ChatFormatting.GREEN));
        return 1;
    }

    // ════════════════════════════════════════════════════════════
    // CLASS COMMAND
    // ════════════════════════════════════════════════════════════

    private static void registerClass(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("class")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ClassGui.openMainMenu(player);
                    return 1;
                })
                .then(Commands.literal("select")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ClassGui.openClassSelection(player);
                            return 1;
                        })
                        .then(Commands.argument("class", StringArgumentType.word())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    String className = StringArgumentType.getString(context, "class").toUpperCase();
                                    
                                    try {
                                        ClassManager.PlayerClass playerClass = ClassManager.PlayerClass.valueOf(className);
                                        ClassManager.selectClass(player, playerClass);
                                    } catch (IllegalArgumentException e) {
                                        player.sendSystemMessage(Component.literal("§cUnknown class: " + className), false);
                                        player.sendSystemMessage(Component.literal("§7Valid classes: WARRIOR, MAGE, RANGER, PALADIN, ROGUE, NECROMANCER, BERSERKER, CLERIC, DRUID, BARD"), false);
                                    }
                                    return 1;
                                })))
                .then(Commands.literal("info")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ClassManager.displayClassInfo(player);
                            return 1;
                        }))
                .then(Commands.literal("list")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ClassManager.displayClassList(player);
                            return 1;
                        }))
                .then(Commands.literal("admin")
                        .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.literal("setclass")
                                        .then(Commands.argument("class", StringArgumentType.word())
                                                .executes(context -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                                    String className = StringArgumentType.getString(context, "class").toUpperCase();
                                                    
                                                    try {
                                                        ClassManager.PlayerClass playerClass = ClassManager.PlayerClass.valueOf(className);
                                                        String uuid = target.getUUID().toString();
                                                        ClassManager.playerClasses.put(uuid, playerClass);
                                                        ClassManager.applyClassEffects(target, playerClass);
                                                        context.getSource().sendSystemMessage(Component.literal("§aSet " + target.getName().getString() + "'s class to " + playerClass.name));
                                                    } catch (IllegalArgumentException e) {
                                                        context.getSource().sendSystemMessage(Component.literal("§cInvalid class: " + className));
                                                    }
                                                    return 1;
                                                })))
                                .then(Commands.literal("addxp")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(context -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                                    int amount = IntegerArgumentType.getInteger(context, "amount");
                                                    ClassManager.awardClassExperience(target, amount);
                                                    context.getSource().sendSystemMessage(Component.literal("§aGranted " + amount + " XP to " + target.getName().getString()));
                                                    return 1;
                                                })))
                                .then(Commands.literal("resetcooldown")
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            String uuid = target.getUUID().toString();
                                            ClassManager.classChangeCooldown.remove(uuid);
                                            context.getSource().sendSystemMessage(Component.literal("§aReset class change cooldown for " + target.getName().getString()));
                                            return 1;
                                        })))));
    }

    // ════════════════════════════════════════════════════════════
    // RECIPE COMMAND
    // ════════════════════════════════════════════════════════════

    private static void registerPoliticalPartyCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerPoliticalPartyRoot(dispatcher, "pp");
        registerPoliticalPartyRoot(dispatcher, "politicalparty");
        registerPoliticalPartyRoot(dispatcher, "party");
    }

    private static void registerPoliticalPartyRoot(CommandDispatcher<CommandSourceStack> dispatcher, String root) {
        dispatcher.register(Commands.literal(root)
                .executes(context -> {
                    ServerPlayer p = context.getSource().getPlayerOrException();
                    PartyJoinGui.open(p);
                    return 1;
                })
                .then(Commands.literal("leader")
                        .executes(context -> {
                            ServerPlayer p = context.getSource().getPlayerOrException();
                            if (PoliticalPartyManager.isPartyLeader(p)) {
                                PartyLeaderGui.open(p);
                            } else {
                                p.sendSystemMessage(Component.literal("Only the party leader can open leader tools. Use /" + root + " for the party menu.")
                                        .withStyle(ChatFormatting.RED), false);
                            }
                            return 1;
                        }))
                .then(Commands.literal("join")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(context -> {
                                    PoliticalPartyManager.tryJoinParty(context.getSource().getPlayerOrException(),
                                            StringArgumentType.getString(context, "id"));
                                    return 1;
                                })))
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(context -> {
                                    PoliticalPartyManager.tryCreateParty(context.getSource().getPlayerOrException(),
                                            StringArgumentType.getString(context, "name"));
                                    return 1;
                                })))
                .then(Commands.literal("donate")
                        .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                .executes(context -> {
                                    long amt = LongArgumentType.getLong(context, "amount");
                                    PoliticalPartyManager.donateCoins(context.getSource().getPlayerOrException(), amt);
                                    return 1;
                                })))
                .then(Commands.literal("platform")
                        .then(Commands.argument("text", StringArgumentType.greedyString())
                                .executes(context -> {
                                    PoliticalPartyManager.trySetPartyPlatform(context.getSource().getPlayerOrException(),
                                            StringArgumentType.getString(context, "text"));
                                    return 1;
                                })))
                .then(Commands.literal("list")
                        .executes(context -> {
                            ServerPlayer p = context.getSource().getPlayerOrException();
                            String ruling = PoliticalPartyManager.getRulingPartyId();
                            for (PoliticalPartyManager.Party party : PoliticalPartyManager.getParties()) {
                                String leader = PoliticalPartyManager.getPartyLeaderUuid(party.id);
                                String ln = leader != null ? DataManager.getPlayerName(leader) : "—";
                                String plat = (party.platform == null || party.platform.isEmpty()) ? "§8(no platform)"
                                        : "§7" + party.platform;
                                String crown = party.id.equals(ruling) ? " §6★" : "";
                                p.sendSystemMessage(Component.literal(party.displayName + crown + " §7(" + party.id + ") §f| §6" + party.budgetCoins
                                        + " §7| leader: §f" + ln).withStyle(ChatFormatting.AQUA), false);
                                p.sendSystemMessage(Component.literal("  " + plat).withStyle(ChatFormatting.GRAY), false);
                            }
                            return 1;
                        }))
                .then(Commands.literal("leave")
                        .executes(context -> {
                            PoliticalPartyManager.leaveParty(context.getSource().getPlayerOrException());
                            return 1;
                        }))
                .then(Commands.literal("council")
                        .then(Commands.literal("yes")
                                .executes(context -> {
                                    PoliticalPartyManager.councilVote(context.getSource().getPlayerOrException(), true);
                                    return 1;
                                }))
                        .then(Commands.literal("no")
                                .executes(context -> {
                                    PoliticalPartyManager.councilVote(context.getSource().getPlayerOrException(), false);
                                    return 1;
                                }))));
    }

    private static void registerLeaderCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("leader")
                .executes(context -> {
                    PartyLeaderGui.open(context.getSource().getPlayerOrException());
                    return 1;
                }));
    }

    private static void registerAscensionCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ascension")
                .executes(context -> {
                    AscensionGrindManager.sendStatus(context.getSource().getPlayerOrException());
                    return 1;
                }));
    }

    private static void registerSectorHeartsCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sectorhearts")
                .executes(context -> {
                    AscensionHeartsGui.openTreePicker(context.getSource().getPlayerOrException());
                    return 1;
                }));
    }

    private static void registerSyndicateCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("syndicate")
                .then(Commands.literal("join")
                        .executes(context -> {
                            DeepstrikeSyndicateManager.tryJoin(context.getSource().getPlayerOrException());
                            return 1;
                        })));
    }

    private static void registerFishing(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /fish - open fishing GUI
        dispatcher.register(Commands.literal("fish")
                .executes(context -> {
                    FishingGui.openMainMenu(context.getSource().getPlayerOrException());
                    return 1;
                })
                .then(Commands.literal("stats")
                        .executes(context -> {
                            FishingGui.sendStats(context.getSource().getPlayerOrException());
                            return 1;
                        }))
                .then(Commands.literal("rod")
                        .executes(context -> {
                            FishingGui.sendRodInfo(context.getSource().getPlayerOrException());
                            return 1;
                        })));

        // /fishing alias
        dispatcher.register(Commands.literal("fishing")
                .executes(context -> {
                    FishingGui.openMainMenu(context.getSource().getPlayerOrException());
                    return 1;
                })
                .then(Commands.literal("stats")
                        .executes(context -> {
                            FishingGui.sendStats(context.getSource().getPlayerOrException());
                            return 1;
                        }))
                .then(Commands.literal("rod")
                        .executes(context -> {
                            FishingGui.sendRodInfo(context.getSource().getPlayerOrException());
                            return 1;
                        })));
    }

}
