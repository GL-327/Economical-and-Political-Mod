package com.political;
import com.political.core.tick.TickDispatcher;
import com.political.recipe.DefaultRecipeBootstrap;



import com.political.bounty.BountySpawnManager;
import com.political.bounty.SlayerArmorHandler;
import com.political.bounty.SlayerManager;
import com.political.bounty.T2ArmorAbilityHandler;
import com.political.economy.AuctionHouseGui;
import com.political.economy.AuctionHouseManager;
import com.political.economy.BazaarManager;
import com.political.economy.CoinManager;
import com.political.economy.CryptoMarket;
import com.political.economy.StockMarket;
import com.political.economy.UndergroundAuctionGui;
import com.political.economy.UndergroundAuctionManager;
import com.political.government.DictatorManager;
import com.political.government.ElectionManager;
import com.political.government.Perk;
import com.political.government.PerkManager;
import com.political.items.ArmorAbilityHandler;
import com.political.items.CustomItemHandler;
import com.political.items.CustomItemManager;
import com.political.items.LegendaryToolHandler;
import com.political.party.PoliticalPartyManager;
import com.political.recipe.RecipeConfigManager;
import com.political.recipe.SlayerRecipes;
import com.political.settlement.SettlementCommands;
import com.political.settlement.SettlementEconomy;
import com.political.settlement.SettlementElectionManager;
import com.political.settlement.SettlementEvents;
import com.political.settlement.SettlementManager;
import com.political.settlement.ShadowGovernmentManager;
import com.political.settlement.WarCommands;
import com.political.settlement.WarManager;
import com.political.stats.HealthScalingManager;
import com.political.stats.PlayerStatsManager;
import com.political.technique.TechniqueManager;
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
    
    public static MinecraftServer server;
    private static TickDispatcher<MinecraftServer, ServerLevel, ServerPlayer> TICKS;

    @Override
    public void onInitializeServer() {
        LOGGER.info("PoliticalServer mod initializing");
        com.political.core.MixinAudit.runIfRequested();
        // Settings first: the tick services read their budgets from them when they are registered.
        var configStore = com.political.core.config.Config.load(
                net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("political").resolve("config.json"));
        if (!configStore.warnings().isEmpty()) {
            LOGGER.warn("config/political/config.json has {} problem(s); see the lines above, the defaults are used for those settings",
                    configStore.warnings().size());
        }
        TICKS = TickRegistrations.create();

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

        CustomItemHandler.register();
        if (com.political.core.config.FeatureSettings.GREAT_HUNT.get()) {
            LivingWorldEvents.register();
        }
        CustomItemHandler.registerGoldOnlyRestrictions();

        // Magic creature death → award Magic XP to killer
        // Death penalty backup (fires even if the mixin's mapped name doesn't match)
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer player) {
                DeathPenaltyManager.onPlayerDeath(player, damageSource);
                com.political.power.game.PowerService.onDeath(player);
            }
            if (!(entity instanceof ServerPlayer)) {
                com.political.power.game.PowerService.onMobDeath(entity);
                Entity mobKiller = damageSource.getEntity();
                if (mobKiller instanceof ServerPlayer killerPlayer) {
                    PoliticalPartyManager.progressPartyCommission(killerPlayer, "KILL_MOBS", 1);
                    GuildManager.onMobKill(killerPlayer, false);
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
            PlayerStatsManager.onPlayerDisconnect(player.getUUID());
            CompanionManager.onLogout(player);
            com.political.power.game.PowerService.onLogout(player);
            com.political.core.gui.ReturnTo.forgetEverywhere(player.getUUID());
            com.political.core.gui.MenuTrail.forget(player.getUUID());
        });

        // Power system: a Domain ends when its caster changes dimension; vows are rechecked when equipment changes.
        net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register(
                (player, origin, destination) -> com.political.power.game.PowerService.onDimensionChange(player));
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.EQUIPMENT_CHANGE.register((entity, slot, previous, current) -> {
            if (entity instanceof ServerPlayer player) {
                com.political.power.game.PowerService.onEquipmentChange(player);
            }
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

        // Every command, registered once through the CommandRegistrar (see ModCommands).
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ModCommands.registerAll(dispatcher));

        // Server lifecycle events
        ServerLifecycleEvents.SERVER_STARTED.register(s -> {
            server = s;
            DataManager.load(s);
            CryptoMarket.persistLoad(s);
            StockMarket.persistLoad(s);
            AuctionHouseManager.load(s);
            UndergroundAuctionManager.load(s);
            ScoreboardManager.init(s);
            RecipeConfigManager.load();
            DefaultRecipeBootstrap.ensureRecipes();
            ProgressionTreeManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            HuntsManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            WorldBossManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            MasteryManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            PartyManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            ClassManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            CustomItemManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            SkyblockTrinkets.ensureRecipes();
            V4RecipeBootstrap.ensureRecipes();
            FlagshipItemRecipeBootstrap.ensureRecipes();
            // ── V5 recipe bootstraps ──
            ExpeditionRewards.bootstrapRecipes();
            GuildRecipeBootstrap.ensureRecipes();
            TradeRecipeBootstrap.ensureRecipes();
            LivingWorldRecipeBootstrap.ensureRecipes();
            ReforgeManager.ensureRecipes();
            RelicManager.ensureRecipes();
            CompanionManager.ensureRecipes();
            RefinedMaterialItems.ensureRecipes();
            CollectionManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            BazaarManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            MinionManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            NemesisManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            CommissionManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            TitleManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            PolymerBridge.initialize();
            CrateManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            TreasuryBootstrap.ensureRecipes();
            AscensionBootstrap.ensureRecipes();
            AscensionHeartSystem.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            SectorHeartRecipeBootstrap.ensureRecipes();
            SlayerRecipes.registerRecipes();
            FishingManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            TechniqueManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            com.political.core.economy.LedgerStore.open(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT), DataManager.LEDGER)
                    .register(com.political.core.persist.StoreRegistry.global());
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
            SettlementEvents.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            SeasonFestivals.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            WorldModifiers.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            RelicManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            CompanionManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            ContractManager.initialize(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            com.political.core.text.TextSelfCheck.run();
            com.political.core.gui.GuiSelfCheck.run();
            LOGGER.info("PoliticalServer data loaded");
            if (com.political.power.game.PowerSmoke.requested()) {
                com.political.power.game.PowerSmoke.schedule();
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
            com.political.core.persist.StoreRegistry.global().saveAllOnShutdown();
            // The eight most expensive tick services of this run, for the M8.1 performance baseline.
            for (String line : TICKS.summaryLines(8)) {
                LOGGER.info("Tick service {}", line);
            }
            CryptoMarket.persistSave(s);
            StockMarket.persistSave(s);
            AscensionHeartSystem.save();
            DataManager.save(s);
            MasteryManager.saveData();
            TechniqueManager.save();
            CollectionManager.save();
            BazaarManager.save();
            MinionManager.save();
            NemesisManager.save();
            CrateManager.save();
            CommissionManager.save();
            TitleManager.save();
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
            SettlementEvents.save();
            SeasonFestivals.save();
            WorldModifiers.save();
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

        // Player join event
        ServerPlayConnectionEvents.JOIN.register((handler, sender, s) -> {
            ServerPlayer player = handler.getPlayer();
            final java.util.UUID joinId = player.getUUID();
            DataManager.registerPlayer(player.getUUID().toString(), player.getName().getString());
            com.political.power.game.PowerService.onJoin(player);
            ProgressionTreeManager.registerPlayer(player.getUUID().toString(), player.getName().getString());
            HuntsManager.registerPlayer(player.getUUID().toString());
            ClassManager.registerPlayer(player.getUUID().toString());
            ClassManager.reapplyClassEffects(player);
            PerkManager.applyActivePerks(player);
            BuffProgressManager.onBankDeposit(player, (long) DataManager.getBankMain(player.getUUID().toString()) + (long) DataManager.getBankSavings(player.getUUID().toString()));
            BuffProgressManager.onPlaytimeUpdate(player, DataManager.getPlaytimeMinutes(player.getUUID().toString()));
            PlayerBuffManager.checkBuffUnlocks(player, player.getUUID().toString());
            PrisonManager.checkPlayerJoin(player);
            TaxManager.checkPlayerJoin(player);
            DictatorManager.checkPlayerJoin(player);
            UndergroundAuctionManager.deliverPendingWins(player);
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

        // All per-tick work is a named service in TickRegistrations; one failing no longer skips the rest.
        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer s) -> {
            server = s;
            TICKS.tick(s);
        });
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

        player.sendSystemMessage(Component.literal("Government status").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));

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

    public static boolean isOperator(ServerPlayer player) {
        return player != null && Commands.hasPermission(Commands.LEVEL_OWNERS)
                .test(player.createCommandSourceStack());
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
    static void syncPlaytimeAndBuffChecks(MinecraftServer s) {
        for (ServerPlayer p : s.getPlayerList().getPlayers()) {
            String u = p.getUUID().toString();
            DataManager.addPlaytimeMinutes(u, 1);
            BuffProgressManager.onPlaytimeUpdate(p, DataManager.getPlaytimeMinutes(u));
            PlayerBuffManager.checkBuffUnlocks(p, u);
        }
    }
}
