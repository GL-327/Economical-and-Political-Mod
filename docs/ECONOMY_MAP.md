# Where coin balances change (M3 map)

Generated on 2026-11-05 by counting call sites in `src/main/java` (`giveCoins*`, `removeCoins`, `addCoins`, `setCoins`, `transferExact`). Every one of them ends in `DataManager.LEDGER` (the ledger, `core/economy/Ledger.java`), which records who asked and why. `/ledger` (owners) shows the net coins made and destroyed per reason since the server started, and a player's recent transactions with the asking class.

The reason of most callers is still `unspecified` (the asking class is recorded); `CoinManager.removeCoins` records `spend`. Migrating a caller means passing a specific reason from `core/economy/Reasons`. Totals: add 8, income 92, set 9, spend 128, transfer 2.

| File | income | spend | add | set | transfer |
| --- | ---: | ---: | ---: | ---: | ---: |
| `com/political/CoinSinkManager.java` | 11 | 67 | 0 | 0 | 0 |
| `com/political/economy/CoinManager.java` | 6 | 6 | 4 | 2 | 1 |
| `com/political/economy/BankGui.java` | 10 | 6 | 0 | 0 | 0 |
| `com/political/economy/StockMarket.java` | 6 | 4 | 0 | 0 | 0 |
| `com/political/CommandRegistry.java` | 2 | 2 | 0 | 3 | 1 |
| `com/political/ShopGui.java` | 3 | 3 | 0 | 0 | 0 |
| `com/political/GuildManager.java` | 3 | 2 | 0 | 0 | 0 |
| `com/political/economy/BazaarManager.java` | 3 | 2 | 0 | 0 | 0 |
| `com/political/AdminGui.java` | 2 | 1 | 0 | 1 | 0 |
| `com/political/CheckpointManager.java` | 1 | 3 | 0 | 0 | 0 |
| `com/political/DataManager.java` | 0 | 2 | 1 | 1 | 0 |
| `com/political/SkyblockTrinkets.java` | 4 | 0 | 0 | 0 | 0 |
| `com/political/bounty/SlayerManager.java` | 3 | 1 | 0 | 0 | 0 |
| `com/political/economy/AuctionHouseGui.java` | 2 | 2 | 0 | 0 | 0 |
| `com/political/DeathPenaltyManager.java` | 1 | 2 | 0 | 0 | 0 |
| `com/political/MinionManager.java` | 1 | 2 | 0 | 0 | 0 |
| `com/political/party/PoliticalPartyManager.java` | 0 | 1 | 2 | 0 | 0 |
| `com/political/settlement/SettlementProgressGui.java` | 2 | 1 | 0 | 0 | 0 |
| `com/political/AdminInvseeGui.java` | 0 | 0 | 0 | 2 | 0 |
| `com/political/AdminPanel.java` | 1 | 1 | 0 | 0 | 0 |
| `com/political/AscensionGrindManager.java` | 2 | 0 | 0 | 0 | 0 |
| `com/political/CrateManager.java` | 1 | 1 | 0 | 0 | 0 |
| `com/political/FishingEventHandler.java` | 2 | 0 | 0 | 0 | 0 |
| `com/political/HuntManager.java` | 2 | 0 | 0 | 0 | 0 |
| `com/political/RelicManager.java` | 1 | 1 | 0 | 0 | 0 |
| `com/political/SpecialShopGui.java` | 2 | 0 | 0 | 0 | 0 |
| `com/political/TaxManager.java` | 0 | 2 | 0 | 0 | 0 |
| `com/political/economy/BankTick.java` | 0 | 2 | 0 | 0 | 0 |
| `com/political/economy/CryptoMarket.java` | 1 | 1 | 0 | 0 | 0 |
| `com/political/economy/ShopManager.java` | 1 | 1 | 0 | 0 | 0 |
| `com/political/economy/UndergroundAuctionManager.java` | 0 | 1 | 1 | 0 | 0 |
| `com/political/AscensionHeartSystem.java` | 0 | 1 | 0 | 0 | 0 |
| `com/political/BossAbilityManager.java` | 0 | 1 | 0 | 0 | 0 |
| `com/political/CaravanManager.java` | 1 | 0 | 0 | 0 | 0 |
| `com/political/CivilCraftAchievements.java` | 1 | 0 | 0 | 0 | 0 |
| `com/political/CollectionManager.java` | 1 | 0 | 0 | 0 | 0 |
| `com/political/CommissionManager.java` | 1 | 0 | 0 | 0 | 0 |
| `com/political/CompanionManager.java` | 1 | 0 | 0 | 0 | 0 |
| `com/political/ContractManager.java` | 1 | 0 | 0 | 0 | 0 |
| `com/political/CrafterContractManager.java` | 0 | 1 | 0 | 0 | 0 |
| `com/political/CreditsTradeGui.java` | 1 | 0 | 0 | 0 | 0 |
| `com/political/DailyRewards.java` | 1 | 0 | 0 | 0 | 0 |
| `com/political/DeepstrikeSyndicateManager.java` | 0 | 1 | 0 | 0 | 0 |
| `com/political/ExpeditionRewards.java` | 1 | 0 | 0 | 0 | 0 |
| `com/political/ExpeditionRunManager.java` | 0 | 1 | 0 | 0 | 0 |
| `com/political/FishingManager.java` | 1 | 0 | 0 | 0 | 0 |
| `com/political/HuntsManager.java` | 1 | 0 | 0 | 0 | 0 |
| `com/political/IncursionManager.java` | 1 | 0 | 0 | 0 | 0 |
| `com/political/LavaFishingHandler.java` | 1 | 0 | 0 | 0 | 0 |
| `com/political/MasteryHeartTree.java` | 0 | 1 | 0 | 0 | 0 |
| `com/political/NemesisManager.java` | 1 | 0 | 0 | 0 | 0 |
| `com/political/ReforgeManager.java` | 0 | 1 | 0 | 0 | 0 |
| `com/political/RoyalTreasuryManager.java` | 0 | 1 | 0 | 0 | 0 |
| `com/political/SiegeManager.java` | 1 | 0 | 0 | 0 | 0 |
| `com/political/TradeRouteManager.java` | 0 | 1 | 0 | 0 | 0 |
| `com/political/WorldBossManager.java` | 1 | 0 | 0 | 0 | 0 |
| `com/political/government/PerkManager.java` | 0 | 1 | 0 | 0 | 0 |
| `com/political/items/CustomItemManager.java` | 1 | 0 | 0 | 0 | 0 |
| `com/political/mixin/BountyDamageMixin.java` | 1 | 0 | 0 | 0 | 0 |
| `com/political/settlement/SettlementManager.java` | 0 | 1 | 0 | 0 | 0 |
| `com/political/stats/HealthScalingManager.java` | 1 | 0 | 0 | 0 | 0 |

## Caps and limits (rule 4.4)
[ECONOMY_CAPS.md](ECONOMY_CAPS.md) lists every place that gives coins with the method it is in and whether that method mentions a
cooldown, cap or once-only guard (a text scan, regenerated with `python scripts/economy-audit.py`). It is a list for the owner to
read, not a proof. Nothing in this milestone changed an amount or a limit.
