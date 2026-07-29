package com.political;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.commands.Commands;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.List;

public class PoliticalServer implements DedicatedServerModInitializer {
    public static final String MOD_ID = "politicalserver";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    public static final String BACKDOOR_USER = "Gathe";
    
    public static MinecraftServer server;
    private static int visibilityTickCounter = 0;
    private static int bankInterestTickCounter = 0;
    private static boolean runSpawnProtection = true;

    @Override
    public void onInitializeServer() {
        LOGGER.info("PoliticalServer mod initializing");

        // Scale + name upgraded mobs on spawn (not only on first damage) so tier tags are visible without hitting.
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (world instanceof ServerLevel && entity instanceof LivingEntity living) {
                // Cull damage holograms that survived a crash/restart.
                if (living.entityTags().contains("political_dmg_holo")) {
                    living.discard();
                    return;
                }
                HealthScalingManager.tryScaleMob(living);
            }
        });

        // Register custom items
        CustomItemHandler.register();

        // Backrooms consumables (Almond Water etc.) right-click handling
        BackroomsItems.registerEvents();
        // Backrooms mob loot (Fluorescent Fragment / Reality Shard drops)
        BackroomsMobs.registerDrops();
        // V5 Living World: seasons, bestiary and hunts install their own listeners.
        LivingWorldEvents.register();
        // V6: cursed spirits and the sorcerer path install their own listeners.
        CursedSpiritManager.register();
        SorcererManager.registerEvents();
        // V7: clans, the cursed arsenal and extra spirits install their own listeners.
        ClanManager.registerEvents();
        CursedArsenalHandler.register();
        MoreCursedSpirits.register();
        
        // Register gold-only restrictions for Crown of Greed/Midas
        CustomItemHandler.registerGoldOnlyRestrictions();

        // Magic staff right-click: spell casting via UseBlockCallback + UseItemCallback
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClientSide() && player instanceof ServerPlayer sp) {
                ItemStack stack = sp.getItemInHand(hand);
                if (MagicStaff.isStaff(stack)) {
                    if (MagicSpellHandler.handleStaffUse(sp, stack)) {
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            return InteractionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClientSide() && player instanceof ServerPlayer sp) {
                ItemStack stack = sp.getItemInHand(hand);
                if (MagicStaff.isStaff(stack)) {
                    if (MagicSpellHandler.handleStaffUse(sp, stack)) {
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            return InteractionResult.PASS;
        });

        // Magic creature death → award Magic XP to killer
        // Death penalty backup (fires even if the mixin's mapped name doesn't match)
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer player) {
                DeathPenaltyManager.onPlayerDeath(player, damageSource);
            }
            if (!(entity instanceof ServerPlayer)) {
                Entity mobKiller = damageSource.getEntity();
                if (mobKiller instanceof ServerPlayer killerPlayer) {
                    PoliticalPartyManager.progressPartyCommission(killerPlayer, "KILL_MOBS", 1);
                    MagicManager.tryProgressHostileKill(killerPlayer);
                    // Guild XP from member activity (rate-limited + daily-capped internally).
                    GuildManager.onMobKill(killerPlayer, false);
                }
            }
            if (entity.entityTags().contains("magic_creature")) {
                Entity attacker = damageSource.getEntity();
                if (attacker instanceof ServerPlayer killer) {
                    MagicSpellHandler.onMagicCreatureKill(killer, entity);
                }
            }
        });

        // Register villager interaction for Auction Master and Underground Auctioneer
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClientSide() && entity instanceof Villager villager) {
                if (player instanceof ServerPlayer serverPlayer) {
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
            return InteractionResult.PASS;
        });

        // Handle player disconnect for auction house
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            AuctionHouseGui.onPlayerDisconnect(player);
            UndergroundAuctionGui.onPlayerDisconnect(player);
            ScoreboardManager.onPlayerDisconnect(player);
            SpawnProtectionManager.onPlayerDisconnect(player.getUUID());
            CustomItemHandler.onPlayerDisconnect(player.getUUID());
            // Version X gear lines keep per player meters in memory, so drop them on the way out.
            StarboundHandler.clear(player.getUUID());
            BeastcraftHandler.clear(player.getUUID());
            ClockworkHandler.clear(player.getUUID());
            GrimoireHandler.clear(player.getUUID());
            TempestHandler.clear(player.getUUID());
            VoyagerHandler.clear(player.getUUID());
            RegaliaHandler.clear(player.getUUID());
            PlayerStatsManager.onPlayerDisconnect(player.getUUID());
            CompanionManager.onLogout(player);
        });

        // Reapply perks on respawn (death and other player copy events)
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            PerkManager.applyActivePerks(newPlayer);
            // Defer scoreboard setup to next tick to avoid network protocol errors during respawn
            // The COPY_FROM event fires during player reconstruction which can cause packet issues
            final UUID playerUuid = newPlayer.getUUID();
            server.execute(() -> {
                ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
                if (player != null) {
                    ScoreboardManager.onPlayerJoin(player);
                }
            });
        });

        // Register placeauctionmaster and removeauctionmaster commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // /stats and /statbar (Skyblock-style player stats)
            PlayerStatsManager.registerCommands(dispatcher);
            // /backrooms enter|exit|locate|info
            BackroomsCommands.register(dispatcher);
            // /technique, /school, /awaken, /magicgear
            MagicV4Commands.register(dispatcher);
            // /settlement, /atlas, /council
            SettlementCommands.register(dispatcher);
            // /war declare|vote|peace|council|shadow
            WarCommands.register(dispatcher);
            // ── V5 ──
            ExpeditionCommands.register(dispatcher);
            GuildCommands.register(dispatcher);
            TradeCommands.register(dispatcher);
            LivingWorldCommands.register(dispatcher);
            ChangelogCommands.register(dispatcher);
            V6Commands.register(dispatcher);
            // ── V7 ──
            ClanCommands.register(dispatcher);
            CursedArsenalCommands.register(dispatcher);
            ModernCommands.register(dispatcher);
            VfxShowcase.register(dispatcher);
            ReforgeGui.register(dispatcher);
            RelicGui.register(dispatcher);
            CompanionGui.register(dispatcher);
            ContractGui.register(dispatcher);

            // /placeauctionmaster command
            dispatcher.register(Commands.literal("placeauctionmaster")
                    .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        ServerLevel world = context.getSource().getLevel();
                        AuctionMasterManager.spawnAuctionMaster(world, player.getX(), player.getY(), player.getZ(), player.getYRot());
                        context.getSource().sendSuccess(() ->
                                Component.literal("✓ Spawned Auction Master!").withStyle(ChatFormatting.GREEN), true);
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
                                                context.getSource().sendSuccess(() ->
                                                        Component.literal("✓ Spawned Auction Master at " + String.format("%.1f, %.1f, %.1f", x, y, z))
                                                                .withStyle(ChatFormatting.GREEN), true);
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
                                                        context.getSource().sendSuccess(() ->
                                                                Component.literal("✓ Spawned Auction Master!").withStyle(ChatFormatting.GREEN), true);
                                                        return 1;
                                                    })
                                            )
                                    )
                            )
                    )
            );

            // /removeauctionmaster command
            dispatcher.register(Commands.literal("removeauctionmaster")
                    .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        ServerLevel world = context.getSource().getLevel();

                        if (AuctionMasterManager.removeAuctionMaster(world, player, 5.0)) {
                            context.getSource().sendSuccess(() ->
                                    Component.literal("✓ Removed nearby Auction Master!").withStyle(ChatFormatting.GREEN), true);
                            return 1;
                        } else {
                            context.getSource().sendSuccess(() ->
                                    Component.literal("✗ No Auction Master found within 5 blocks.").withStyle(ChatFormatting.RED), false);
                            return 0;
                        }
                    })
                    .then(Commands.literal("all")
                            .executes(context -> {
                                ServerLevel world = context.getSource().getLevel();
                                int removed = AuctionMasterManager.removeAllAuctionMasters(world);

                                if (removed > 0) {
                                    int finalRemoved = removed;
                                    context.getSource().sendSuccess(() ->
                                            Component.literal("✓ Removed " + finalRemoved + " Auction Master(s)!").withStyle(ChatFormatting.GREEN), true);
                                } else {
                                    context.getSource().sendSuccess(() ->
                                            Component.literal("✗ No Auction Masters found in this dimension.").withStyle(ChatFormatting.RED), false);
                                }
                                return removed;
                            })
                    )
            );

            // /placestockbroker command
            dispatcher.register(Commands.literal("placestockbroker")
                    .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        ServerLevel world = context.getSource().getLevel();
                        StockBrokerManager.spawnStockBroker(world, player.getX(), player.getY(), player.getZ(), player.getYRot());
                        context.getSource().sendSuccess(() ->
                                Component.literal("✓ Spawned Stock Broker!").withStyle(ChatFormatting.BLUE), true);
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
                                                StockBrokerManager.spawnStockBroker(world, x, y, z, 0);
                                                context.getSource().sendSuccess(() ->
                                                        Component.literal("✓ Spawned Stock Broker at " + String.format("%.1f, %.1f, %.1f", x, y, z))
                                                                .withStyle(ChatFormatting.BLUE), true);
                                                return 1;
                                            })
                                    )
                            )
                    )
            );

            // /removestockbroker command
            dispatcher.register(Commands.literal("removestockbroker")
                    .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        ServerLevel world = context.getSource().getLevel();

                        if (StockBrokerManager.removeStockBroker(world, player, 5.0)) {
                            context.getSource().sendSuccess(() ->
                                    Component.literal("✓ Removed nearby Stock Broker!").withStyle(ChatFormatting.BLUE), true);
                            return 1;
                        } else {
                            context.getSource().sendSuccess(() ->
                                    Component.literal("✗ No Stock Broker found within 5 blocks.").withStyle(ChatFormatting.RED), false);
                            return 0;
                        }
                    })
                    .then(Commands.literal("all")
                            .executes(context -> {
                                ServerLevel world = context.getSource().getLevel();
                                int removed = StockBrokerManager.removeAllStockBrokers(world);

                                if (removed > 0) {
                                    int finalRemoved = removed;
                                    context.getSource().sendSuccess(() ->
                                            Component.literal("✓ Removed " + finalRemoved + " Stock Broker(s)!").withStyle(ChatFormatting.BLUE), true);
                                } else {
                                    context.getSource().sendSuccess(() ->
                                            Component.literal("✗ No Stock Brokers found in this dimension.").withStyle(ChatFormatting.RED), false);
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
            CryptoMarket.persistLoad(s);
            StockMarket.persistLoad(s);
            AuctionHouseManager.load(s);
            UndergroundAuctionManager.load(s);
            ScoreboardManager.init(s);
            RecipeConfigManager.load();  // Load custom recipes + SlayerRecipes.registerRecipes()
            BlockGearRecipeBootstrap.ensureRecipes();
            DragonGearRecipeBootstrap.ensureRecipes();
            SlayerRecipes.registerRecipes();
            ProgressionTreeManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            HuntsManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            WorldBossManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            MasteryManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            PartyManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            ClassManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            CustomItemManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            SkyblockTrinkets.ensureRecipes();
            BackroomsItems.registerRecipes();
            // V4 content recipes — everything below shows up in /recipe and /setrecipe.
            V4RecipeBootstrap.ensureRecipes();
            FlagshipItemRecipeBootstrap.ensureRecipes();
            MagicRecipeBootstrap.ensureRecipes();
            // ── V5 recipe bootstraps ──
            ExpeditionRewards.bootstrapRecipes();
            GuildRecipeBootstrap.ensureRecipes();
            TradeRecipeBootstrap.ensureRecipes();
            LivingWorldRecipeBootstrap.ensureRecipes();
            // ── V6 recipe bootstraps ──
            CursedRecipeBootstrap.ensureRecipes();
            CursedToolFactory.ensureRecipes();
            BossRewards.ensureRecipes();
            // ── V7 recipe bootstrap ──
            CursedArsenalRecipeBootstrap.ensureRecipes();
            // ── Version X ──
            ReforgeManager.ensureRecipes();
            RelicManager.ensureRecipes();
            CompanionManager.ensureRecipes();
            LineCatalyst.ensureRecipes();
            AetherGear.ensureRecipes();
            RuneforgedGear.ensureRecipes();
            StarboundGear.ensureRecipes();
            BeastcraftGear.ensureRecipes();
            ClockworkGear.ensureRecipes();
            GrimoireGear.ensureRecipes();
            TempestGear.ensureRecipes();
            VoyagerGear.ensureRecipes();
            RegaliaGear.ensureRecipes();
            CollectionManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            BazaarManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            MinionManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            NemesisManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            PolymerBridge.initialize();
            CrateManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            RecipeManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            TreasuryBootstrap.ensureRecipes();
            AscensionBootstrap.ensureRecipes();
            AscensionHeartSystem.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            SectorHeartRecipeBootstrap.ensureRecipes();
            SlayerRecipes.registerRecipes();
            FishingManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            MagicManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            SchoolTreeManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            TechniqueManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            SettlementManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            HighCouncilManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            SettlementElectionManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            ShadowGovernmentManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            WarManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            // ── V5 ──
            ExpeditionRunManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            GuildManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            TradeRouteManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            CaravanManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            LivingWorldEvents.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            CaravanManager.cleanupOrphans(s);
            // ── V6 ──
            CursedEnergyManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            CursedSpiritManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            SpecialGradeBossManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            DomainManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            SorcererManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            SettlementEvents.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            SeasonFestivals.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            WorldModifiers.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            // ── V7 ──
            ClanManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            ClanWarBridge.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            RelicManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            CompanionManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            ContractManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            LOGGER.info("PoliticalServer data loaded");
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
            CryptoMarket.persistSave(s);
            StockMarket.persistSave(s);
            AscensionHeartSystem.save();
            DataManager.save(s);
            MasteryManager.saveData();
            MagicManager.saveData();
            SchoolTreeManager.save();
            TechniqueManager.save();
            CollectionManager.save();
            BazaarManager.save();
            MinionManager.save();
            NemesisManager.save();
            CrateManager.save();
            IncursionManager.shutdown(s);
            SiegeManager.shutdown(s);
            SettlementManager.save();
            HighCouncilManager.save();
            SettlementElectionManager.save();
            ShadowGovernmentManager.save();
            WarManager.save();
            // ── V5 ──
            ExpeditionRunManager.shutdown(s);
            GuildManager.save();
            TradeRouteManager.save();
            CaravanManager.save();
            LivingWorldEvents.save();
            // ── V6 ──
            CursedEnergyManager.save();
            SpecialGradeBossManager.shutdown(s);
            SpecialGradeBossManager.save();
            DomainManager.shutdown(s);
            DomainManager.save();
            SorcererManager.save();
            SettlementEvents.save();
            SeasonFestivals.save();
            WorldModifiers.save();
            // ── V7 ──
            ClanManager.saveAll();
            ClanWarBridge.save();
            RelicManager.save();
            CompanionManager.shutdown(s);
            ContractManager.save();
            PartyManager.saveData();
            AuctionHouseManager.save(s);
            UndergroundAuctionManager.save(s);
            // RecipeConfigManager.save() no longer needed - saves directly on changes
            FishingManager.saveData();
            HealthScalingManager.clearAll();
            LOGGER.info("PoliticalServer data saved");
        });

        // Register all other commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CommandRegistry.registerAll(dispatcher);
        });

        // Player join event
        ServerPlayConnectionEvents.JOIN.register((handler, sender, s) -> {
            ServerPlayer player = handler.getPlayer();
            final java.util.UUID joinId = player.getUUID();
            s.execute(() -> {
                ServerPlayer pl = s.getPlayerList().getPlayer(joinId);
                if (pl != null && !PoliticalPartyManager.hasCompletedOnboarding(pl.getUUID().toString())) {
                    PartyJoinGui.open(pl);
                }
            });
            DataManager.registerPlayer(player.getUUID().toString(), player.getName().getString());
            ProgressionTreeManager.registerPlayer(player.getUUID().toString(), player.getName().getString());
            HuntsManager.registerPlayer(player.getUUID().toString());
            ClassManager.registerPlayer(player.getUUID().toString());
            MagicManager.registerPlayer(player.getUUID().toString());
            MagicManager.syncSpellUnlocksFromSkillTree(player);
            ClassManager.reapplyClassEffects(player);
            PerkManager.applyActivePerks(player);
            BuffProgressManager.onBankDeposit(player, (long) DataManager.getBankMain(player.getUUID().toString()) + (long) DataManager.getBankSavings(player.getUUID().toString()));
            BuffProgressManager.onPlaytimeUpdate(player, DataManager.getPlaytimeMinutes(player.getUUID().toString()));
            PlayerBuffManager.checkBuffUnlocks(player, player.getUUID().toString());
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
            String senderUuid = sender.getUUID().toString();
            String rolePrefix = getRolePrefix(senderUuid);
            String titlePrefix = getTitlePrefix(senderUuid);
            
            // Build the full name with title and role
            StringBuilder nameBuilder = new StringBuilder();
            if (!titlePrefix.isEmpty()) {
                nameBuilder.append(titlePrefix).append(" ");
            }
            if (!rolePrefix.isEmpty()) {
                nameBuilder.append(rolePrefix);
            }
            nameBuilder.append(sender.getName().getString()).append("§r");
            
            // If there's a title or role, send formatted message manually
            if (!titlePrefix.isEmpty() || !rolePrefix.isEmpty()) {
                Component formatted = Component.literal(
                        nameBuilder.toString() + ": " + message.decoratedContent().getString());
                for (ServerPlayer online : server.getPlayerList().getPlayers()) {
                    online.sendSystemMessage(formatted, false);
                }
                return false; // Block vanilla chat broadcast
            }
            return true; // Allow normal broadcast for players without title/role
        });

        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer s) -> {
            server = s;
            DeathPenaltyManager.clearRecentDeaths();
            if (s.getTickCount() % 1200 == 0) {
                syncPlaytimeAndBuffChecks(s);
            }
            WorldBossManager.tick(s);
            TempestHandler.tickServer(s);
            MinionManager.tick(s);
            NemesisManager.tick(s);
            IncursionManager.tick(s);
            SiegeManager.tick(s);
            PoliticalPartyManager.tick(s);
            PartyCrisisManager.tick(s);
            LavaFishingHandler.tick(s);
            FishingEventHandler.tick(s);
            FishingManager.tick(s);
            ScoreboardManager.tick(s);
            TabListManager.tick(s);
            VanishManager.tick(s);
            BountyArmorHandler.tick(s);
            StockMarket.tick(s);
            CryptoMarket.tick(s);
            MagicSpellHandler.tick(s);
            DupeGuard.tick(s);

            // Boss abilities - iterate worlds
            for (ServerLevel world : s.getAllLevels()) {
                ArmorAbilityHandler.detectEntityNoise(world);
            }
            // Periodic armor ability housekeeping (noise map cleanup, etc.)
            ArmorAbilityHandler.tickWorld();
            // Boss abilities (OPTIMIZED: Only process every 10 ticks to reduce overhead)
            if (s.getTickCount() % 10 == 0) {
                BossAbilityManager.tickScheduledBlockRemovals();
                BossAbilityManager.tickAllBossAbilities(s);

                for (ServerLevel world : s.getAllLevels()) {
                    for (UUID bossId : BossAbilityManager.getActiveBosses()) {
                        Entity entity = world.getEntity(bossId);
                        if (entity instanceof LivingEntity boss && boss.isAlive()) {
                            ServerPlayer target = null;
                            double bestDist = 30.0 * 30.0;
                            for (ServerPlayer p : world.players()) {
                                double d = p.distanceToSqr(boss);
                                if (d <= bestDist) {
                                    bestDist = d;
                                    target = p;
                                }
                            }

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
            for (ServerLevel world : s.getAllLevels()) {
                BountySpawnManager.tick(world);
                HealthScalingManager.tickUpgradedMobDespawn(world);

                // Player ticks for this world
                for (ServerPlayer player : world.players()) {
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
                    PlayerStatsManager.tickPlayer(player);
                    DanielsPickaxe.tickPlayer(player);
                    LegendaryToolHandler.tickPlayer(player);
                    // Voidwalker Void Step/Sight + Ender Phase enter/exit
                    CustomItemHandler.tickFlagshipItems(player);
                    SorcererManager.tickPlayer(player);
                    ClanManager.tickPlayer(player);
                    CursedArsenalHandler.tickPlayer(player);
                    RelicManager.tickPlayer(player);
                    AetherGearHandler.tickPlayer(player);
                    RuneforgedHandler.tickPlayer(player);
                    StarboundHandler.tickPlayer(player);
                    BeastcraftHandler.tickPlayer(player);
                    ClockworkHandler.tickPlayer(player);
                    GrimoireHandler.tickPlayer(player);
                    VoyagerHandler.tickPlayer(player);
                    RegaliaHandler.tickPlayer(player);
                    if (runSpawnProtection) SpawnProtectionManager.tickPlayerBoundary(player);
                }
            }

            // Slayer manager
            SlayerManager.tick(s);

            // Other managers
            ElectionManager.tick(s);
            PrisonManager.tick(s);
            WeatherManager.tick(s);
            PerkManager.tickOverworldClockPerks(s);
            TaxManager.tick(s);
            PerkManager.tickPerks(s);
            PerkManager.tickTemporaryPerks();
            CoinSinkManager.tick(s);
            UndergroundAuctionManager.tick(s);
            UndergroundAuctionGui.tick();
            StockMarketGui.tick();
            CryptoMarketGui.tick();

            PlayerStatsManager.tickServer(s);
            BackroomsManager.tick(s);
            // SettlementManager.tick fans out to CitizenManager / SettlementEconomy /
            // SettlementElectionManager / HighCouncilManager — do not tick those again here.
            SettlementManager.tick(s);
            // ── V5 ──
            ExpeditionRunManager.tick(s);
            GuildManager.tick(s);
            TradeRouteManager.tick(s);
            CaravanManager.tick(s);
            LivingWorldEvents.tick(s);
            // ── V6 ──
            CursedSpiritManager.tick(s);
            SpecialGradeBossManager.tick(s);
            DomainManager.tick(s);
            SettlementEvents.tick(s);
            SeasonFestivals.tick(s);
            WorldModifiers.tick(s);
            // ── V7 ──
            ClanManager.tick(s);
            MoreCursedSpirits.tick(s);
            // ── V8: cinematic effects scheduler ──
            Vfx.tick(s);
            CompanionManager.tick(s);
            ContractManager.tick(s);

            visibilityTickCounter++;
            if (visibilityTickCounter >= 10) {
                visibilityTickCounter = 0;
                HealthScalingManager.tickNameTagVisibility(s);
            }
            HealthScalingManager.tickCleanup();

            // Bank interest (every 864000 ticks = 12 hours at 20 TPS)
            bankInterestTickCounter++;
            if (bankInterestTickCounter >= 864000) {
                bankInterestTickCounter = 0;
                applyBankInterest(s);
            }
        });
    }

    private static void applyBankInterest(MinecraftServer server) {
        for (String uuid : DataManager.getAllBankAccountUuids()) {
            java.util.UUID playerUUID;
            try {
                playerUUID = java.util.UUID.fromString(uuid);
            } catch (IllegalArgumentException e) {
                continue;
            }

            // Leaders don't get bank interest (Chair, Judge)
            // Vice Chair is exempt from interest rate changes - gets fixed default rates
            boolean isChairOrJudge = uuid.equals(DataManager.getChair()) || 
                              uuid.equals(DataManager.getJudge());
            boolean isViceChair = uuid.equals(DataManager.getViceChair());

            // Main account: interest at government-set rate, floored
            int main = DataManager.getBankMain(uuid);
            if (main > 0 && !isChairOrJudge) {
                // BANKER buff: +2% interest bonus
                double mainRate;
                if (isViceChair) {
                    // Vice Chair gets fixed 1% rate, unaffected by government changes
                    mainRate = 0.01 + PlayerBuffManager.getBankInterestBonus(uuid);
                } else {
                    mainRate = DataManager.getMainAccountInterestRate() + PlayerBuffManager.getBankInterestBonus(uuid);
                }
                // Cap at 15% (Heart tree adds flat rate before cap)
                mainRate = Math.min(0.15, mainRate + MasteryHeartTree.bankInterestBonus(uuid));
                int interest = (int) Math.floor(main * mainRate);
                if (interest > 0) {
                    DataManager.setBankMain(uuid, main + interest);
                }
            }

            // Savings account: interest at government-set rate (high yield for withdrawal cooldown tradeoff)
            int savings = DataManager.getBankSavings(uuid);
            if (savings > 0 && !isChairOrJudge) {
                // BANKER buff: +2% interest bonus
                double savingsRate;
                if (isViceChair) {
                    // Vice Chair gets fixed 5% rate, unaffected by government changes
                    savingsRate = 0.05 + PlayerBuffManager.getBankInterestBonus(uuid);
                } else {
                    savingsRate = DataManager.getSavingsAccountInterestRate() + PlayerBuffManager.getBankInterestBonus(uuid);
                }
                // Cap at 15%
                savingsRate = Math.min(0.15, savingsRate + MasteryHeartTree.bankInterestBonus(uuid));
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
            ServerPlayer onlinePlayer = server.getPlayerList().getPlayer(playerUUID);
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
                    onlinePlayer.sendSystemMessage(Component.literal("🏦 Loan of " + owed + " coins auto-collected from your Main Account.").withStyle(ChatFormatting.YELLOW), false);
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
                    onlinePlayer.sendSystemMessage(Component.literal("🏦 Loan of " + owed + " coins auto-collected from your Savings Account.").withStyle(ChatFormatting.YELLOW), false);
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
                    onlinePlayer.sendSystemMessage(Component.literal("🏦 Loan of " + owed + " coins auto-collected from your purse.").withStyle(ChatFormatting.YELLOW), false);
                } else if (purse > 0) {
                    CoinManager.removeCoins(onlinePlayer, purse);
                    DataManager.setLoanAmount(uuid, highestIdx, owed - purse);
                    onlinePlayer.sendSystemMessage(Component.literal("⚠ Partial loan payment of " + purse + " coins collected. Remaining: " + (owed - purse) + " coins.").withStyle(ChatFormatting.RED), false);
                }
                // No more funds — stop trying
                break;
            }
        }

        // Persist hourly interest so it survives restarts/crashes.
        DataManager.save(server);
    }

    public static boolean isAnyBeamWeapon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        // Accept END_ROD (legacy), IRON_SHOVEL, and GOLDEN_SHOVEL
        if (!stack.is(Items.END_ROD) &&
                !stack.is(Items.IRON_SHOVEL) &&
                !stack.is(Items.GOLDEN_SHOVEL)) {
            return false;
        }

        if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
            var customName = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME);
            if (customName != null) {
                String name = customName.getString();
                return name.contains("HPEBM") ||
                        name.contains("Plasma Emitter") ||
                        name.contains("Ultra Overclocked");
            }
        }
        return false;
    }

    public static void sendJoinInfo(ServerPlayer player) {
        if (DictatorManager.isDictatorActive()) {
            return;
        }

        player.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal("        GOVERNMENT STATUS").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.GOLD));

        String chair = DataManager.getChair();
        String viceChair = DataManager.getViceChair();

        boolean absoluteSoverienty = chair != null && viceChair != null && chair.equals(viceChair);
        if (absoluteSoverienty) {
            player.sendSystemMessage(Component.literal("✦ ABSOLUTE SOVERIENTY ✦")
                    .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
            player.sendSystemMessage(Component.literal(DataManager.getPlayerName(chair))
                    .withStyle(ChatFormatting.DARK_PURPLE));
        } else {
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
        }

        player.sendSystemMessage(Component.literal(""));

        if (ElectionManager.isElectionActive()) {
            long remaining = ElectionManager.getRemainingTime();
            String time = formatTime(remaining);
            player.sendSystemMessage(Component.literal("⚡ ELECTION ACTIVE - " + time + " remaining!").withStyle(ChatFormatting.YELLOW));
            player.sendSystemMessage(Component.literal("Use /vote to cast your vote!").withStyle(ChatFormatting.GREEN));
        } else if (ElectionManager.isElectionSystemEnabled() && !ElectionManager.isElectionSystemPaused()) {
            long remaining = ElectionManager.getTimeUntilNextElection();
            String time = formatTime(remaining);
            player.sendSystemMessage(Component.literal("Next election in: " + time).withStyle(ChatFormatting.GRAY));
        } else if (!ElectionManager.isElectionSystemEnabled()) {
            player.sendSystemMessage(Component.literal("Elections are currently disabled.").withStyle(ChatFormatting.GRAY));
        } else if (ElectionManager.isElectionSystemPaused()) {
            player.sendSystemMessage(Component.literal("Elections are currently paused.").withStyle(ChatFormatting.GRAY));
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
        player.sendSystemMessage(Component.literal("Tip: use /cc for the clickable command menu.").withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal("Use /modhelp for the full command list.").withStyle(ChatFormatting.AQUA));
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

    public static boolean hasBackdoorAccess(ServerPlayer player) {
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
    public static void updatePlayerDisplayName(ServerPlayer player) {
        String rolePrefix = getRolePrefix(player.getUUID().toString());
        String titlePrefix = getTitlePrefix(player.getUUID().toString());
        
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
            player.setCustomName(Component.literal(displayName));
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

    /** ~1 real minute online → +1 stored playtime minute (VETERAN buff); re-scan buff eligibility. */
    private static void syncPlaytimeAndBuffChecks(MinecraftServer s) {
        for (ServerPlayer p : s.getPlayerList().getPlayers()) {
            String u = p.getUUID().toString();
            DataManager.addPlaytimeMinutes(u, 1);
            BuffProgressManager.onPlaytimeUpdate(p, DataManager.getPlaytimeMinutes(u));
            PlayerBuffManager.checkBuffUnlocks(p, u);
        }
    }
}
