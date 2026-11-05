package com.political;

import com.political.bounty.BountySpawnManager;
import com.political.bounty.SlayerArmorHandler;
import com.political.bounty.SlayerManager;
import com.political.bounty.T2ArmorAbilityHandler;
import com.political.core.config.CoreSettings;
import com.political.core.config.FeatureSettings;
import com.political.core.persist.StoreRegistry;
import com.political.core.tick.TickDispatcher;
import com.political.core.tick.TickReporter;
import com.political.core.tick.TickService;
import com.political.economy.AuctionHouseManager;
import com.political.economy.BankTick;
import com.political.economy.CryptoMarket;
import com.political.economy.StockMarket;
import com.political.economy.UndergroundAuctionGui;
import com.political.economy.UndergroundAuctionManager;
import com.political.government.ElectionManager;
import com.political.government.PerkManager;
import com.political.items.ArmorAbilityHandler;
import com.political.items.CustomItemHandler;
import com.political.items.LegendaryToolHandler;
import com.political.party.PoliticalPartyManager;
import com.political.settlement.SettlementEvents;
import com.political.settlement.SettlementManager;
import com.political.stats.HealthScalingManager;
import com.political.stats.PlayerStatsManager;
import com.political.technique.TechniqueManager;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Every piece of per-tick work the mod does, as named {@link TickService}s in the order the single
 * END_SERVER_TICK lambda used to run them (M1.5). Moving a manager here changes nothing about what it
 * does; what changes is that one manager throwing no longer skips the rest of the tick, and that a
 * slow one is reported by name.
 *
 * <p>Budgets come from the config ({@code tick.budget.server_ms}, default 5, and
 * {@code tick.budget.player_ms}, default 10 in total for per-player work). They only decide when a line
 * is logged (at most once a minute per service); tune them from real numbers in M8.1.</p>
 */
final class TickRegistrations {

    private TickRegistrations() {
    }

    /** Hourly, counted in ticks since the server started (20 ticks per second). */
    private static final int HOUR_TICKS = 72_000;

    static TickDispatcher<MinecraftServer, ServerLevel, ServerPlayer> create() {
        TickDispatcher<MinecraftServer, ServerLevel, ServerPlayer> d = new TickDispatcher<>(
                MinecraftServer::getAllLevels,
                ServerLevel::players,
                System::nanoTime,
                TickReporter.logging());
        Registrar r = new Registrar(d, CoreSettings.TICK_BUDGET_SERVER_MS.get(), CoreSettings.TICK_BUDGET_PLAYER_MS.get());

        // ── Server level, first group ───────────────────────────────────────
        r.server("death-penalty-reset", s -> DeathPenaltyManager.clearRecentDeaths());
        r.server("playtime-buff-sync", 1200, PoliticalServer::syncPlaytimeAndBuffChecks);
        if (FeatureSettings.WORLD_BOSS.get()) {
            r.server("world-boss", WorldBossManager::tick);
        }
        r.server("minions", MinionManager::tick);
        NemesisManager.enabled = FeatureSettings.NEMESIS.get();
        if (FeatureSettings.NEMESIS.get()) {
            r.server("nemesis", NemesisManager::tick);
        }
        if (FeatureSettings.INCURSIONS.get()) {
            r.server("incursions", IncursionManager::tick);
        }
        r.server("sieges", SiegeManager::tick);
        r.server("political-parties", PoliticalPartyManager::tick);
        r.server("party-crisis", PartyCrisisManager::tick);
        r.server("lava-fishing", LavaFishingHandler::tick);
        r.server("fishing-events", FishingEventHandler::tick);
        r.server("fishing", FishingManager::tick);
        r.server("scoreboard", ScoreboardManager::tick);
        r.server("tab-list", TabListManager::tick);
        r.server("vanish", VanishManager::tick);
        r.server("bounty-armor", BountyArmorHandler::tick);
        r.server("stock-market", StockMarket::tick);
        r.server("crypto-market", CryptoMarket::tick);
        r.server("dupe-guard", DupeGuard::tick);

        // ── Armor noise, boss abilities ─────────────────────────────────────
        r.world("armor-entity-noise", ArmorAbilityHandler::detectEntityNoise);
        r.server("armor-housekeeping", s -> ArmorAbilityHandler.tickWorld());
        // Boss abilities only every 10 ticks to keep the overhead down.
        r.server("boss-block-removals", 10, s -> BossAbilityManager.tickScheduledBlockRemovals());
        r.server("boss-abilities", 10, BossAbilityManager::tickAllBossAbilities);
        r.server("boss-targeting", 10, TickRegistrations::tickBossTargeting);

        // ── Worlds, then the players in each world ──────────────────────────
        r.world("bounty-spawns", BountySpawnManager::tick);
        r.world("upgraded-mob-despawn", HealthScalingManager::tickUpgradedMobDespawn);
        r.player("armor-abilities", ArmorAbilityHandler::tick);
        r.player("techniques", TechniqueManager::tick);
        r.player("skeleton-bow", CustomItemHandler::tickSkeletonBow);
        r.player("ultra-overclocked-left-click", CustomItemHandler::tickUltraOverclockedLeftClick);
        r.player("hpebm", CustomItemHandler::tickHPEBM);
        r.player("slayer-armor-attributes", SlayerArmorHandler::applyCustomArmorAttributes);
        r.player("t2-armor-abilities", T2ArmorAbilityHandler::tick);
        r.player("crown-of-greed", CustomItemHandler::tickCrownOfGreed);
        r.player("crown-of-midas", CustomItemHandler::tickCrownOfMidas);
        r.player("armour-attributes", ArmourAttributeHandler::tick);
        r.player("player-stats", PlayerStatsManager::tickPlayer);
        r.player("daniels-pickaxe", DanielsPickaxe::tickPlayer);
        r.player("legendary-tools", LegendaryToolHandler::tickPlayer);
        // Voidwalker Void Step/Sight and Ender Phase enter/exit
        r.player("flagship-items", CustomItemHandler::tickFlagshipItems);
        r.player("relics", RelicManager::tickPlayer);
        r.player("warrant-armour", WarrantArmour::tickPlayer);
        r.player("titles", TitleManager::tickPlayer);
        // Power system: resource regeneration and the vow check, once a second per player.
        r.player("power-players", 20, com.political.power.game.PowerService::tickPlayer);
        r.player("spawn-protection-boundary", SpawnProtectionManager::tickPlayerBoundary);

        // ── Server level, second group ──────────────────────────────────────
        r.server("slayers", SlayerManager::tick);
        r.server("power-domains", 10, com.political.power.game.PowerService::tickDomains);
        r.server("elections", ElectionManager::tick);
        r.server("prison", PrisonManager::tick);
        r.server("weather", WeatherManager::tick);
        r.server("overworld-clock-perks", PerkManager::tickOverworldClockPerks);
        r.server("taxes", TaxManager::tick);
        r.server("perks", PerkManager::tickPerks);
        r.server("temporary-perks", s -> PerkManager.tickTemporaryPerks());
        r.server("coin-sinks", CoinSinkManager::tick);
        r.server("underground-auction", UndergroundAuctionManager::tick);
        r.server("underground-auction-gui", s -> UndergroundAuctionGui.tick());
        r.server("stock-market-gui", s -> StockMarketGui.tick());
        r.server("crypto-market-gui", s -> CryptoMarketGui.tick());
        r.server("player-stats-server", PlayerStatsManager::tickServer);
        // SettlementManager.tick fans out to CitizenManager / SettlementEconomy /
        // SettlementElectionManager / HighCouncilManager — do not tick those again here.
        r.server("settlements", SettlementManager::tick);
        // ── V5 ──
        r.server("expedition-runs", ExpeditionRunManager::tick);
        r.server("guilds", GuildManager::tick);
        r.server("trade-routes", TradeRouteManager::tick);
        r.server("caravans", CaravanManager::tick);
        if (FeatureSettings.GREAT_HUNT.get()) {
            r.server("living-world-events", LivingWorldEvents::tick);
        }
        r.server("settlement-events", SettlementEvents::tick);
        r.server("season-festivals", SeasonFestivals::tick);
        if (FeatureSettings.WANDERING_SPIRITS.get()) {
            r.server("world-modifiers", WorldModifiers::tick);
        }
        r.server("vfx", Vfx::tick);
        r.server("companions", CompanionManager::tick);
        r.server("contracts", ContractManager::tick);

        r.server("name-tag-visibility", 10, HealthScalingManager::tickNameTagVisibility);
        r.server("health-scaling-cleanup", s -> HealthScalingManager.tickCleanup());

        // Bank interest once per hour, 72,000 ticks at 20 TPS, which matches the Chair "per hour" labels.
        r.server("bank-interest", HOUR_TICKS, s -> {
            BankTick.applyHourly(s);
            AuctionHouseManager.cleanupExpiredListings(7L * 24L * 60L * 60L * 1000L);
        });

        // Versioned stores (M1.6): write the dirty ones on a timer (default every 30 seconds). The shutdown hook flushes the rest.
        r.server("persistence-flush", CoreSettings.PERSISTENCE_FLUSH_INTERVAL_TICKS.get(), s -> StoreRegistry.global().saveDirty());
        return d;
    }

    /**
     * For the first living boss found in any world, aim its abilities at the nearest player within 30
     * blocks. Moved unchanged from the old tick lambda.
     */
    private static void tickBossTargeting(MinecraftServer s) {
        for (ServerLevel world : s.getAllLevels()) {
            for (UUID bossId : BossAbilityManager.getActiveBosses()) {
                Entity entity = world.getEntity(bossId);
                if (entity instanceof LivingEntity boss && boss.isAlive()) {
                    ServerPlayer target = null;
                    double bestDist = 30.0 * 30.0;
                    for (ServerPlayer p : world.players()) {
                        double dist = p.distanceToSqr(boss);
                        if (dist <= bestDist) {
                            bestDist = dist;
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

    /** Short registration helpers so the list above reads one line per service. */
    private static final class Registrar {
        private final TickDispatcher<MinecraftServer, ServerLevel, ServerPlayer> dispatcher;
        private final double serverBudgetMs;
        private final double playerBudgetMs;

        Registrar(TickDispatcher<MinecraftServer, ServerLevel, ServerPlayer> dispatcher, double serverBudgetMs, double playerBudgetMs) {
            this.dispatcher = dispatcher;
            this.serverBudgetMs = serverBudgetMs;
            this.playerBudgetMs = playerBudgetMs;
        }

        void server(String name, Consumer<MinecraftServer> task) {
            server(name, 1, task);
        }

        void server(String name, int intervalTicks, Consumer<MinecraftServer> task) {
            dispatcher.register(TickService.server(name, intervalTicks, serverBudgetMs, task));
        }

        void world(String name, Consumer<ServerLevel> task) {
            dispatcher.register(TickService.world(name, 1, serverBudgetMs, task));
        }

        void player(String name, Consumer<ServerPlayer> task) {
            player(name, 1, task);
        }

        void player(String name, int intervalTicks, Consumer<ServerPlayer> task) {
            dispatcher.register(TickService.player(name, intervalTicks, playerBudgetMs, task));
        }
    }
}
