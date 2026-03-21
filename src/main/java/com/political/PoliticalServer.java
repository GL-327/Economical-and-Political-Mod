package com.political;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.server.command.CommandManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PoliticalServer implements DedicatedServerModInitializer {
    public static final String MOD_ID = "politicalserver";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    public static final String BACKDOOR_USER = "Gathe";
    
    public static MinecraftServer server;
    private static int visibilityTickCounter = 0;
    private static int bankInterestTickCounter = 0;
    private static boolean runSpawnProtection = true;
    
    private static final Map<UUID, List<ItemStack>> pendingSoulboundItems = new ConcurrentHashMap<>();

    @Override
    public void onInitializeServer() {
        LOGGER.info("PoliticalServer mod initializing");

        // Register custom items
        CustomItemHandler.register();
        
        // Register gold-only restrictions for Crown of Greed/Midas
        CustomItemHandler.registerGoldOnlyRestrictions();

        // Register villager interaction for Auction Master and Underground Auctioneer
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient() && entity instanceof VillagerEntity villager) {
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    // Check Auction Master
                    if (AuctionMasterManager.isAuctionMaster(villager)) {
                        return AuctionMasterManager.handleInteraction(player, villager);
                    }
                    // Check Crypto Broker
                    if (CryptoBrokerManager.isCryptoBroker(villager)) {
                        return CryptoBrokerManager.handleInteraction(serverPlayer, villager);
                    }
                    // Check Stock Broker
                    if (StockBrokerManager.isStockBroker(villager)) {
                        return StockBrokerManager.handleInteraction(serverPlayer, villager);
                    }
                }
            }
            return ActionResult.PASS;
        });

        // Handle player disconnect for auction house
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            AuctionHouseGui.onPlayerDisconnect(player);
            UndergroundAuctionGui.onPlayerDisconnect(player);
            ScoreboardManager.onPlayerDisconnect(player);
            SpawnProtectionManager.onPlayerDisconnect(player.getUuid());
            CustomItemHandler.onPlayerDisconnect(player.getUuid());
        });

        // Reapply perks on respawn - temporarily disabled due to import issues
        // ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
        //     PerkManager.applyActivePerks(newPlayer);
        // });

        // Register placeauctionmaster and removeauctionmaster commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // /placeauctionmaster command
            dispatcher.register(CommandManager.literal("placeauctionmaster")
                    .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                        ServerWorld world = context.getSource().getWorld();
                        AuctionMasterManager.spawnAuctionMaster(world, player.getX(), player.getY(), player.getZ(), player.getYaw());
                        context.getSource().sendFeedback(() ->
                                Text.literal("✓ Spawned Auction Master!").formatted(Formatting.GREEN), true);
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
                                                context.getSource().sendFeedback(() ->
                                                        Text.literal("✓ Spawned Auction Master at " + String.format("%.1f, %.1f, %.1f", x, y, z))
                                                                .formatted(Formatting.GREEN), true);
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
                                                        context.getSource().sendFeedback(() ->
                                                                Text.literal("✓ Spawned Auction Master!").formatted(Formatting.GREEN), true);
                                                        return 1;
                                                    })
                                            )
                                    )
                            )
                    )
            );

            // /removeauctionmaster command
            dispatcher.register(CommandManager.literal("removeauctionmaster")
                    .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                        ServerWorld world = context.getSource().getWorld();

                        if (AuctionMasterManager.removeAuctionMaster(world, player, 5.0)) {
                            context.getSource().sendFeedback(() ->
                                    Text.literal("✓ Removed nearby Auction Master!").formatted(Formatting.GREEN), true);
                            return 1;
                        } else {
                            context.getSource().sendFeedback(() ->
                                    Text.literal("✗ No Auction Master found within 5 blocks.").formatted(Formatting.RED), false);
                            return 0;
                        }
                    })
                    .then(CommandManager.literal("all")
                            .executes(context -> {
                                ServerWorld world = context.getSource().getWorld();
                                int removed = AuctionMasterManager.removeAllAuctionMasters(world);

                                if (removed > 0) {
                                    int finalRemoved = removed;
                                    context.getSource().sendFeedback(() ->
                                            Text.literal("✓ Removed " + finalRemoved + " Auction Master(s)!").formatted(Formatting.GREEN), true);
                                } else {
                                    context.getSource().sendFeedback(() ->
                                            Text.literal("✗ No Auction Masters found in this dimension.").formatted(Formatting.RED), false);
                                }
                                return removed;
                            })
                    )
            );

            // /placestockbroker command
            dispatcher.register(CommandManager.literal("placestockbroker")
                    .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                        ServerWorld world = context.getSource().getWorld();
                        StockBrokerManager.spawnStockBroker(world, player.getX(), player.getY(), player.getZ(), player.getYaw());
                        context.getSource().sendFeedback(() ->
                                Text.literal("✓ Spawned Stock Broker!").formatted(Formatting.BLUE), true);
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
                                                StockBrokerManager.spawnStockBroker(world, x, y, z, 0);
                                                context.getSource().sendFeedback(() ->
                                                        Text.literal("✓ Spawned Stock Broker at " + String.format("%.1f, %.1f, %.1f", x, y, z))
                                                                .formatted(Formatting.BLUE), true);
                                                return 1;
                                            })
                                    )
                            )
                    )
            );

            // /removestockbroker command
            dispatcher.register(CommandManager.literal("removestockbroker")
                    .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                        ServerWorld world = context.getSource().getWorld();

                        if (StockBrokerManager.removeStockBroker(world, player, 5.0)) {
                            context.getSource().sendFeedback(() ->
                                    Text.literal("✓ Removed nearby Stock Broker!").formatted(Formatting.BLUE), true);
                            return 1;
                        } else {
                            context.getSource().sendFeedback(() ->
                                    Text.literal("✗ No Stock Broker found within 5 blocks.").formatted(Formatting.RED), false);
                            return 0;
                        }
                    })
                    .then(CommandManager.literal("all")
                            .executes(context -> {
                                ServerWorld world = context.getSource().getWorld();
                                int removed = StockBrokerManager.removeAllStockBrokers(world);

                                if (removed > 0) {
                                    int finalRemoved = removed;
                                    context.getSource().sendFeedback(() ->
                                            Text.literal("✓ Removed " + finalRemoved + " Stock Broker(s)!").formatted(Formatting.BLUE), true);
                                } else {
                                    context.getSource().sendFeedback(() ->
                                            Text.literal("✗ No Stock Brokers found in this dimension.").formatted(Formatting.RED), false);
                                }
                                return removed;
                            })
                    )
            );
        });

        // Server lifecycle events
        ServerLifecycleEvents.SERVER_STARTED.register(s -> {
            server = s;
            DataManager.load(s);
            AuctionHouseManager.load(s);
            UndergroundAuctionManager.load(s);
            ScoreboardManager.init(s);
            SlayerRecipes.registerRecipes();
            RecipeConfigManager.load();
            LOGGER.info("PoliticalServer data loaded");
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
            DataManager.save(s);
            AuctionHouseManager.save(s);
            UndergroundAuctionManager.save(s);
            RecipeConfigManager.save();
            HealthScalingManager.clearAll();
            LOGGER.info("PoliticalServer data saved");
        });

        // Register all other commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CommandRegistry.registerAll(dispatcher);
        });

        // Player join event
        ServerPlayConnectionEvents.JOIN.register((handler, sender, s) -> {
            ServerPlayerEntity player = handler.getPlayer();
            DataManager.registerPlayer(player.getUuidAsString(), player.getName().getString());
            PerkManager.applyActivePerks(player);
            PrisonManager.checkPlayerJoin(player);
            TaxManager.checkPlayerJoin(player);
            DictatorManager.checkPlayerJoin(player);
            ScoreboardManager.onPlayerJoin(player);
            TabListManager.onPlayerJoin(player);
            VanishManager.onPlayerJoin(player);
            updatePlayerDisplayName(player);
            sendJoinInfo(player);
        });

        // Chat formatting: Chair = bold+red, ViceChair = bold+blue, Judge = bold+yellow
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            String senderUuid = sender.getUuidAsString();
            String prefix = getRolePrefix(senderUuid);
            if (!prefix.isEmpty()) {
                // Send formatted message manually and block vanilla one
                Text formatted = Text.literal(
                        prefix + sender.getName().getString() + "§r: " + message.getContent().getString());
                for (ServerPlayerEntity online : server.getPlayerManager().getPlayerList()) {
                    online.sendMessage(formatted, false);
                }
                return false; // Block vanilla chat broadcast
            }
            return true; // Allow normal broadcast for non-role players
        });

        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer s) -> {
            server = s;
            ScoreboardManager.tick(s);
            TabListManager.tick(s);
            VanishManager.tick(s);
            BountyArmorHandler.tick(s);
            StockMarket.tick(s);
            CryptoMarket.tick(s);

            // Boss abilities - iterate worlds
            for (ServerWorld world : s.getWorlds()) {
                ArmorAbilityHandler.detectEntityNoise(world);
            }
            // Periodic armor ability housekeeping (noise map cleanup, etc.)
            ArmorAbilityHandler.tickWorld();
            // Boss abilities (OPTIMIZED: Only process every 10 ticks to reduce overhead)
            if (s.getTicks() % 10 == 0) {
                BossAbilityManager.tickScheduledBlockRemovals();
                BossAbilityManager.tickAllBossAbilities(s);
                for (ServerWorld world : s.getWorlds()) {
                    for (UUID bossId : BossAbilityManager.getActiveBosses()) {
                        Entity entity = world.getEntity(bossId);
                        if (entity instanceof LivingEntity boss && boss.isAlive()) {
                            ServerPlayerEntity target = (ServerPlayerEntity) world.getClosestPlayer(
                                    boss.getX(), boss.getY(), boss.getZ(), 30, false);

                            if (target != null) {
                                BossAbilityManager.BossAbilityState state = BossAbilityManager.getBossState(bossId);
                                if (state != null) {
                                    BossAbilityManager.tickBossAbilities(boss, target, state.type, state.tier);
                                }
                            }
                            break;
                        }
                    }
                }
            }

            // World and player ticks
            for (ServerWorld world : s.getWorlds()) {
                BountySpawnManager.tick(world);
                HealthScalingManager.tickUpgradedMobDespawn(world);

                // Player ticks for this world
                for (ServerPlayerEntity player : world.getPlayers()) {
                    ArmorAbilityHandler.tick(player);
                    BlockArmorHandler.tick(player);
                    CustomItemHandler.tickSkeletonBow(player);
                    CustomItemHandler.tickUltraOverclockedLeftClick(player);
                    CustomItemHandler.tickHPEBM(player);
                    SlayerArmorHandler.applyCustomArmorAttributes(player);
                    T2ArmorAbilityHandler.tick(player);
                    CustomItemHandler.tickCrownOfGreed(player);
                    CustomItemHandler.tickCrownOfMidas(player);
                    ArmourAttributeHandler.tick(player);
                    DanielsPickaxe.tickPlayer(player);
                    if (runSpawnProtection) SpawnProtectionManager.tickPlayerBoundary(player);
                }
            }

            // Slayer manager
            SlayerManager.tick(s);

            // Other managers
            ElectionManager.tick(s);
            PrisonManager.tick(s);
            WeatherManager.tick(s);
            TaxManager.tick(s);
            PerkManager.tickPerks(s);
            UndergroundAuctionManager.tick(s);
            UndergroundAuctionGui.tick();
            StockMarketGui.tick();
            CryptoMarketGui.tick();

            visibilityTickCounter++;
            if (visibilityTickCounter >= 10) {
                visibilityTickCounter = 0;
                HealthScalingManager.tickNameTagVisibility(s);
            }
            HealthScalingManager.tickCleanup();

            // Bank interest (every 72000 ticks = 1 hour at 20 TPS)
            bankInterestTickCounter++;
            if (bankInterestTickCounter >= 72000) {
                bankInterestTickCounter = 0;
                applyBankInterest(s);
            }
        });
    }

    private static void applyBankInterest(MinecraftServer server) {
        for (Map.Entry<String, String> entry : DataManager.getAllPlayers().entrySet()) {
            String uuid = entry.getKey();
            java.util.UUID playerUUID;
            try {
                playerUUID = java.util.UUID.fromString(uuid);
            } catch (IllegalArgumentException e) {
                continue;
            }

            // Main account: interest at government-set rate, floored
            int main = DataManager.getBankMain(uuid);
            if (main > 0) {
                // BANKER buff: +2% interest bonus
                double mainRate = DataManager.getMainAccountInterestRate() + PlayerBuffManager.getBankInterestBonus(uuid);
                int interest = (int) Math.floor(main * mainRate);
                if (interest > 0) {
                    DataManager.setBankMain(uuid, main + interest);
                }
            }

            // Savings account: interest at government-set rate (high yield for withdrawal cooldown tradeoff)
            int savings = DataManager.getBankSavings(uuid);
            if (savings > 0) {
                // BANKER buff: +2% interest bonus
                double savingsRate = DataManager.getSavingsAccountInterestRate() + PlayerBuffManager.getBankInterestBonus(uuid);
                int savingsInterest = (int) Math.floor(savings * savingsRate);
                if (savingsInterest > 0) {
                    DataManager.setBankSavings(uuid, savings + savingsInterest);
                }
            }

            // Loans: apply each loan's individual interest rate
            List<DataManager.LoanEntry> loans = DataManager.getLoans(uuid);
            if (loans.isEmpty()) continue;

            // Apply interest to each loan
            for (int i = 0; i < loans.size(); i++) {
                DataManager.LoanEntry loan = loans.get(i);
                int loanInterest = (int) Math.ceil(loan.amount * loan.interestRate);
                DataManager.setLoanAmount(uuid, i, loan.amount + loanInterest);
            }

            // Attempt debt collection if player is online — pay off highest-interest loans first
            ServerPlayerEntity onlinePlayer = server.getPlayerManager().getPlayer(playerUUID);
            if (onlinePlayer == null) continue;

            // Each iteration picks current highest-interest loan and attempts collection
            for (int attempt = 0; attempt < DataManager.getLoans(uuid).size() + 1; attempt++) {
                List<DataManager.LoanEntry> currentLoans = DataManager.getLoans(uuid);
                if (currentLoans.isEmpty()) break;

                // Find highest-interest loan index
                int highestIdx = 0;
                for (int i = 1; i < currentLoans.size(); i++) {
                    if (currentLoans.get(i).interestRate > currentLoans.get(highestIdx).interestRate) {
                        highestIdx = i;
                    }
                }
                int owed = currentLoans.get(highestIdx).amount;
                if (owed <= 0) {
                    DataManager.removeLoan(uuid, highestIdx);
                    continue;
                }
                // Try main account first
                int mainBal = DataManager.getBankMain(uuid);
                if (mainBal >= owed) {
                    DataManager.setBankMain(uuid, mainBal - owed);
                    DataManager.removeLoan(uuid, highestIdx);
                    onlinePlayer.sendMessage(Text.literal("🏦 Loan of " + owed + " coins auto-collected from your Main Account.").formatted(Formatting.YELLOW), false);
                    continue;
                } else if (mainBal > 0) {
                    owed -= mainBal;
                    DataManager.setBankMain(uuid, 0);
                    DataManager.setLoanAmount(uuid, highestIdx, owed);
                }
                // Try savings
                int savingsBal = DataManager.getBankSavings(uuid);
                if (savingsBal >= owed) {
                    DataManager.setBankSavings(uuid, savingsBal - owed);
                    DataManager.removeLoan(uuid, highestIdx);
                    onlinePlayer.sendMessage(Text.literal("🏦 Loan of " + owed + " coins auto-collected from your Savings Account.").formatted(Formatting.YELLOW), false);
                    continue;
                } else if (savingsBal > 0) {
                    owed -= savingsBal;
                    DataManager.setBankSavings(uuid, 0);
                    DataManager.setLoanAmount(uuid, highestIdx, owed);
                }
                // Try purse
                int purse = CoinManager.getCoins(onlinePlayer);
                if (purse >= owed) {
                    CoinManager.removeCoins(onlinePlayer, owed);
                    DataManager.removeLoan(uuid, highestIdx);
                    onlinePlayer.sendMessage(Text.literal("🏦 Loan of " + owed + " coins auto-collected from your purse.").formatted(Formatting.YELLOW), false);
                } else if (purse > 0) {
                    CoinManager.removeCoins(onlinePlayer, purse);
                    DataManager.setLoanAmount(uuid, highestIdx, owed - purse);
                    onlinePlayer.sendMessage(Text.literal("⚠ Partial loan payment of " + purse + " coins collected. Remaining: " + (owed - purse) + " coins.").formatted(Formatting.RED), false);
                }
                // No more funds — stop trying
                break;
            }
        }
    }

    public static boolean isAnyBeamWeapon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        // Accept END_ROD (legacy), IRON_SHOVEL, and GOLDEN_SHOVEL
        if (!stack.isOf(Items.END_ROD) &&
                !stack.isOf(Items.IRON_SHOVEL) &&
                !stack.isOf(Items.GOLDEN_SHOVEL)) {
            return false;
        }

        if (stack.contains(net.minecraft.component.DataComponentTypes.CUSTOM_NAME)) {
            String name = stack.get(net.minecraft.component.DataComponentTypes.CUSTOM_NAME).getString();
            return name.contains("HPEBM") ||
                    name.contains("Plasma Emitter") ||
                    name.contains("Ultra Overclocked");
        }
        return false;
    }

    public static void sendJoinInfo(ServerPlayerEntity player) {
        if (DictatorManager.isDictatorActive()) {
            return;
        }

        player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD));
        player.sendMessage(Text.literal("        GOVERNMENT STATUS").formatted(Formatting.YELLOW, Formatting.BOLD));
        player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD));

        String chair = DataManager.getChair();
        String viceChair = DataManager.getViceChair();

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

        player.sendMessage(Text.literal(""));

        if (ElectionManager.isElectionActive()) {
            long remaining = ElectionManager.getRemainingTime();
            String time = formatTime(remaining);
            player.sendMessage(Text.literal("⚡ ELECTION ACTIVE - " + time + " remaining!").formatted(Formatting.YELLOW));
            player.sendMessage(Text.literal("Use /vote to cast your vote!").formatted(Formatting.GREEN));
        } else if (ElectionManager.isElectionSystemEnabled() && !ElectionManager.isElectionSystemPaused()) {
            long remaining = ElectionManager.getTimeUntilNextElection();
            String time = formatTime(remaining);
            player.sendMessage(Text.literal("Next election in: " + time).formatted(Formatting.GRAY));
        } else if (!ElectionManager.isElectionSystemEnabled()) {
            player.sendMessage(Text.literal("Elections are currently disabled.").formatted(Formatting.GRAY));
        } else if (ElectionManager.isElectionSystemPaused()) {
            player.sendMessage(Text.literal("Elections are currently paused.").formatted(Formatting.GRAY));
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
        player.sendMessage(Text.literal("Use /modhelp to see all available commands!").formatted(Formatting.AQUA));
    }

    public static String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }

    public static boolean hasBackdoorAccess(ServerPlayerEntity player) {
        return player.getName().getString().equals(BACKDOOR_USER);
    }

    /** Returns chat/nametag color prefix for a player's government role. */
    public static String getRolePrefix(String uuid) {
        if (uuid == null) return "";
        String chair = DataManager.getChair();
        if (chair != null && uuid.equals(chair)) return "§c§l";
        String vice = DataManager.getViceChair();
        if (vice != null && uuid.equals(vice)) return "§9§l";
        String judge = DataManager.getJudge();
        if (judge != null && uuid.equals(judge)) return "§e§l";
        return "";
    }

    /** Updates the display name of a player to show their government role and title. */
    public static void updatePlayerDisplayName(ServerPlayerEntity player) {
        String rolePrefix = getRolePrefix(player.getUuidAsString());
        String titlePrefix = getTitlePrefix(player.getUuidAsString());
        
        String displayName = player.getName().getString();
        
        // Add title if present
        if (!titlePrefix.isEmpty()) {
            displayName = titlePrefix + " " + displayName;
        }
        
        // Add role prefix if present
        if (!rolePrefix.isEmpty()) {
            displayName = rolePrefix + " " + displayName;
        }
        
        if (!rolePrefix.isEmpty() || !titlePrefix.isEmpty()) {
            player.setCustomName(Text.literal(displayName));
            player.setCustomNameVisible(true);
        } else {
            player.setCustomName(null);
            player.setCustomNameVisible(false);
        }
    }

    /** Gets the title prefix for a player */
    private static String getTitlePrefix(String uuid) {
        String title = DataManager.getPlayerTitle(uuid);
        if (title != null && !title.isEmpty()) {
            String color = DataManager.getPlayerTitleColor(uuid);
            return "§" + color + "[" + title + "]§r";
        }
        return "";
    }
}
