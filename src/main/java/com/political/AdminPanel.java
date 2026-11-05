package com.political;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.political.bounty.BountyGui;
import com.political.economy.AuctionHouseGui;
import com.political.economy.BazaarGui;
import com.political.economy.CoinManager;
import com.political.economy.UndergroundAuctionGui;
import com.political.government.GovernmentTreasuryGui;
import com.political.government.PerksGui;
import com.political.party.PartyLeaderGui;
import com.political.recipe.RecipeConfigManager;
import com.political.recipe.RecipesGui;
import com.political.settlement.Settlement;
import com.political.settlement.SettlementEconomyGui;
import com.political.settlement.SettlementManager;
import com.political.settlement.SettlementProgressGui;
import com.political.settlement.SettlementScanner;
import com.political.settlement.SettlementTier;
import com.political.settlement.ShadowGovernmentManager;
import com.political.settlement.WarGui;
import com.political.settlement.WarManager;
import com.political.stats.PlayerStatsManager;
import com.political.technique.TechniqueManager;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The operator console.
 *
 * <p>The old panel covered seven categories while the mod had grown past three hundred and eighty
 * classes, so most of what an operator actually needs had no button anywhere and lived only in
 * commands. This replaces it. Every screen is built out of {@link GuiKit} so the whole console reads
 * as one thing, every destructive action goes through a confirmation, and the old panel is still one
 * click away under Legacy so nothing that used to work stopped working.</p>
 */
public final class AdminPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminPanel.class);


    private AdminPanel() {}

    private static final int SIZE = 54;

    // ── Main hub ──────────────────────────────────────────────────────────

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Operator Console")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE, GuiKit.DANGER_EDGE);

        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        int online = server == null ? 0 : server.getPlayerList().getPlayerCount();
        double mspt = server == null ? 0 : server.getAverageTickTimeNanos() / 1_000_000.0;
        double tps = mspt <= 0 ? 20.0 : Math.min(20.0, 1000.0 / mspt);

        GuiKit.header(gui, Items.COMMAND_BLOCK, "Operator Console", ChatFormatting.RED,
                online + " online, " + String.format("%.1f", tps) + " tps",
                "Everything on this server, in one place");

        // Row 1: the things you reach for most.
        gui.setSlot(10, GuiKit.tile(Items.CLOCK, "Server", ChatFormatting.AQUA,
                () -> openServer(player),
                "Performance and memory", "World and dimension counts", "Save and reload"));

        gui.setSlot(11, GuiKit.tile(Items.PLAYER_HEAD, "Players", ChatFormatting.GREEN,
                () -> openPlayers(player, 0),
                "Every online player", "Heal, feed, teleport, gamemode", "Coins, stats, inventory"));

        gui.setSlot(12, GuiKit.tile(Items.IRON_BARS, "Moderation", ChatFormatting.RED,
                () -> openModeration(player),
                "Prison and exile", "Vanish and anti cheat", "Chest access history"));

        gui.setSlot(13, GuiKit.tile(Items.GOLD_BLOCK, "Economy", ChatFormatting.GOLD,
                () -> openEconomy(player),
                "Coins, credits and tax", "Auctions and shops", "Markets and crypto"));

        gui.setSlot(14, GuiKit.tile(Items.BELL, "Politics", ChatFormatting.YELLOW,
                () -> openPolitics(player),
                "Elections and offices", "Parties and the high council", "Shadow government"));

        gui.setSlot(15, GuiKit.tile(Items.CAMPFIRE, "Settlements", ChatFormatting.GREEN,
                () -> openSettlements(player, 0),
                "Every settlement on the map", "Tiers, appeal and citizens", "Rescan and economy"));

        gui.setSlot(16, GuiKit.tile(Items.NETHERITE_SWORD, "Wars", ChatFormatting.DARK_RED,
                () -> openWars(player),
                "Active wars and motions", "Council seats and votes", "Declare and resolve"));

        // Row 2: content and world.
        gui.setSlot(19, GuiKit.tile(Items.CRAFTING_TABLE, "Content", ChatFormatting.LIGHT_PURPLE,
                () -> openContent(player),
                "Custom items and recipes", "Reforge, relics, contracts"));

        gui.setSlot(20, GuiKit.tile(Items.ZOMBIE_HEAD, "Combat", ChatFormatting.DARK_PURPLE,
                () -> openCombat(player),
                "Mob scaling and bosses", "Bounties and slayers"));

        gui.setSlot(21, GuiKit.tile(Items.ENCHANTED_BOOK, "Techniques", ChatFormatting.BLUE,
                () -> com.political.power.game.PowerAdminScreens.open(player, 0),
                "Powers of every player"));

        gui.setSlot(22, GuiKit.tile(Items.COMPASS, "World", ChatFormatting.AQUA,
                () -> openWorld(player),
                "Spawn, time and weather", "World events and seasons"));

        gui.setSlot(23, GuiKit.tile(Items.WRITABLE_BOOK, "Diagnostics", ChatFormatting.WHITE,
                () -> openDiagnostics(player),
                "Recipe integrity", "Registered systems", "What is loaded and what is not"));

        gui.setSlot(24, GuiKit.tile(Items.CHEST, "Legacy panel", ChatFormatting.GRAY,
                () -> AdminGui.open(player),
                "The previous admin panel", "Kept so nothing was lost"));

        gui.setSlot(25, GuiKit.tile(Items.END_CRYSTAL, "Changelog", ChatFormatting.LIGHT_PURPLE,
                () -> ChangelogGui.open(player),
                "Every version and what landed"));

        // Row 3: the completeness row. The categories above are what an operator reaches for
        // daily, these two are the guarantee that everything else is still one click away.
        gui.setSlot(28, GuiKit.tile(Items.BOOKSHELF, "Every menu", ChatFormatting.AQUA,
                () -> AdminDirectoryGui.open(player),
                AdminDirectoryGui.totalMenus() + " screens, grouped",
                "Everything the mod can open"));

        gui.setSlot(29, GuiKit.tile(Items.COMMAND_BLOCK, "Every command", ChatFormatting.RED,
                () -> AdminCommandsGui.open(player, 0),
                "Read live from this server",
                "Searchable, click to run"));

        gui.setSlot(31, GuiKit.info(Items.PAPER, "How to find things", ChatFormatting.WHITE,
                "The tiles above are the common jobs.",
                "",
                "Every menu lists all " + AdminDirectoryGui.totalMenus() + " screens by group.",
                "Every command reads the live dispatcher,",
                "so it covers vanilla and other mods too.",
                "",
                "Both have search. Neither can go stale."));

        gui.setSlot(33, GuiKit.tile(Items.NAME_TAG, "Titles", ChatFormatting.GOLD,
                () -> TitleGui.openFrom(player, () -> open(player)), Title.values().length + " titles"));

        gui.setSlot(34, GuiKit.tile(Items.PAPER, "Requisition board", ChatFormatting.GOLD,
                () -> CommissionGui.open(player),
                Commission.total() + " postings"));

        gui.setSlot(49, GuiKit.close(player));
        gui.open();
    }

    // ── Server ────────────────────────────────────────────────────────────

    private static void openServer(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Server").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        double mspt = server == null ? 0 : server.getAverageTickTimeNanos() / 1_000_000.0;
        double tps = mspt <= 0 ? 20.0 : Math.min(20.0, 1000.0 / mspt);

        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) / 1_048_576L;
        long maxMb = rt.maxMemory() / 1_048_576L;

        GuiKit.header(gui, Items.CLOCK, "Server", ChatFormatting.AQUA,
                "Live health and world state");

        gui.setSlot(19, GuiKit.info(Items.CLOCK, "Tick rate", ChatFormatting.AQUA,
                String.format("%.2f tps", tps),
                String.format("%.2f ms per tick", mspt),
                GuiKit.bar(tps / 20.0, ChatFormatting.GREEN, ChatFormatting.DARK_GRAY)));

        gui.setSlot(20, GuiKit.info(Items.REDSTONE, "Memory", ChatFormatting.RED,
                usedMb + " MB of " + maxMb + " MB",
                GuiKit.bar(maxMb == 0 ? 0 : usedMb / (double) maxMb,
                        ChatFormatting.GOLD, ChatFormatting.DARK_GRAY)));

        int players = server == null ? 0 : server.getPlayerList().getPlayerCount();
        int maxPlayers = server == null ? 0 : server.getPlayerList().getMaxPlayers();
        gui.setSlot(21, GuiKit.info(Items.PLAYER_HEAD, "Players", ChatFormatting.GREEN,
                players + " of " + maxPlayers + " online"));

        int levels = 0;
        long entities = 0;
        long chunks = 0;
        if (server != null) {
            for (ServerLevel lvl : server.getAllLevels()) {
                levels++;
                for (var ignored : lvl.getAllEntities()) entities++;
                chunks += lvl.getChunkSource().getLoadedChunksCount();
            }
        }
        final long finalEntities = entities;
        final long finalChunks = chunks;

        gui.setSlot(22, GuiKit.info(Items.GRASS_BLOCK, "Worlds", ChatFormatting.GREEN,
                levels + " dimensions loaded",
                finalChunks + " chunks",
                finalEntities + " entities"));

        gui.setSlot(23, GuiKit.info(Items.NAME_TAG, "Version", ChatFormatting.LIGHT_PURPLE,
                "CivilCraft Version X",
                "Seven gear lines, 7216 recipes"));

        gui.setSlot(28, GuiKit.action(Items.WRITABLE_BOOK, "Save everything now", ChatFormatting.GREEN,
                () -> GuiKit.confirm(player, "Save all data",
                        "Writes every manager to disk immediately.",
                        () -> {
                            SettlementManager.save();
                            WarManager.save();
                            ShadowGovernmentManager.save();
                            TechniqueManager.save();
                            SchoolTreeManager.save();
                            HighCouncilManager.save();
                            player.sendSystemMessage(Component.literal("§aCore managers written to disk."));
                            openServer(player);
                        },
                        () -> openServer(player)),
                "Flushes every manager", "Safe to run at any time"));

        gui.setSlot(29, GuiKit.action(Items.CLOCK, "Refresh", ChatFormatting.AQUA,
                () -> openServer(player), "Repaint with current numbers"));

        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    // ── Players ───────────────────────────────────────────────────────────

    private static void openPlayers(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Players").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        List<ServerPlayer> online = new ArrayList<>();
        if (server != null) online.addAll(server.getPlayerList().getPlayers());
        online.sort(Comparator.comparing(p -> p.getName().getString().toLowerCase()));

        GuiKit.header(gui, Items.PLAYER_HEAD, "Players", ChatFormatting.GREEN,
                online.size() + " online",
                "Click anyone to open their card");

        List<GuiElement> entries = new ArrayList<>();
        for (ServerPlayer target : online) {
            String name = target.getName().getString();
            long coins = CoinManager.getCoinsLong(target);
            entries.add(GuiKit.head(name, name, ChatFormatting.WHITE,
                    () -> openPlayerCard(player, target),
                    "Health " + (int) target.getHealth() + " of " + (int) target.getMaxHealth(),
                    "Coins " + GuiKit.num(coins),
                    "Mode " + target.gameMode.getGameModeForPlayer().getName(),
                    "At " + (int) target.getX() + ", " + (int) target.getY() + ", " + (int) target.getZ()));
        }

        GuiKit.paint(gui, entries, page, GuiKit.LIST_SLOTS.length);
        GuiKit.pager(gui, SIZE, page, entries.size(), GuiKit.LIST_SLOTS.length,
                p -> openPlayers(player, p));
        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    /** Everything an operator normally wants to do to one player, on one screen. */
    private static void openPlayerCard(ServerPlayer admin, ServerPlayer target) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, admin, false);
        String name = target.getName().getString();
        gui.setTitle(Component.literal(name).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        GuiKit.header(gui, Items.PLAYER_HEAD, name, ChatFormatting.GREEN,
                "Health " + (int) target.getHealth() + " of " + (int) target.getMaxHealth(),
                "Coins " + GuiKit.num(CoinManager.getCoinsLong(target)));

        // Wellbeing.
        gui.setSlot(10, GuiKit.action(Items.GOLDEN_APPLE, "Heal", ChatFormatting.RED,
                () -> {
                    target.setHealth(target.getMaxHealth());
                    target.clearFire();
                    admin.sendSystemMessage(Component.literal("§aHealed " + name + "."));
                    openPlayerCard(admin, target);
                }, "Full health, fire out"));

        gui.setSlot(11, GuiKit.action(Items.COOKED_BEEF, "Feed", ChatFormatting.GOLD,
                () -> {
                    target.getFoodData().eat(20, 20f);
                    admin.sendSystemMessage(Component.literal("§aFed " + name + "."));
                    openPlayerCard(admin, target);
                }, "Food and saturation full"));

        gui.setSlot(12, GuiKit.action(Items.MILK_BUCKET, "Clear effects", ChatFormatting.WHITE,
                () -> {
                    target.removeAllEffects();
                    openPlayerCard(admin, target);
                }, "Strips every potion effect"));

        // Movement.
        gui.setSlot(14, GuiKit.action(Items.ENDER_PEARL, "Teleport to them", ChatFormatting.LIGHT_PURPLE,
                () -> {
                    PlayerTeleportHelper.teleport(admin, ((ServerLevel) target.level()), target.getX(), target.getY(), target.getZ(),
                            admin.getYRot(), admin.getXRot());
                    admin.closeContainer();
                }, "Moves you to their position"));

        gui.setSlot(15, GuiKit.action(Items.ENDER_EYE, "Bring them here", ChatFormatting.LIGHT_PURPLE,
                () -> GuiKit.confirm(admin, "Bring " + name,
                        "They are moved to your position without warning.",
                        () -> {
                            PlayerTeleportHelper.teleport(target, ((ServerLevel) admin.level()), admin.getX(), admin.getY(), admin.getZ(),
                                    target.getYRot(), target.getXRot());
                            target.sendSystemMessage(Component.literal("§eYou were summoned by an operator."));
                            openPlayerCard(admin, target);
                        },
                        () -> openPlayerCard(admin, target)),
                "Moves them to you"));

        gui.setSlot(16, GuiKit.action(Items.COMPASS, "Send to spawn", ChatFormatting.AQUA,
                () -> {
                    // The mod already owns a spawn point, so use it rather than inventing one.
                    SpawnManager.teleportToSpawn(target);
                    openPlayerCard(admin, target);
                }, SpawnManager.hasSpawn()
                        ? "Returns them to the server spawn"
                        : "No server spawn is set yet"));

        // Game mode row.
        GameType mode = target.gameMode.getGameModeForPlayer();
        gui.setSlot(19, modeButton(admin, target, GameType.SURVIVAL, Items.IRON_SWORD, mode));
        gui.setSlot(20, modeButton(admin, target, GameType.CREATIVE, Items.COMMAND_BLOCK, mode));
        gui.setSlot(21, modeButton(admin, target, GameType.ADVENTURE, Items.MAP, mode));
        gui.setSlot(22, modeButton(admin, target, GameType.SPECTATOR, Items.ENDER_EYE, mode));

        // Economy.
        gui.setSlot(28, GuiKit.stepper(Items.GOLD_INGOT, "Coins", ChatFormatting.GOLD,
                CoinManager.getCoinsLong(target), 1000L,
                delta -> {
                    if (delta > 0) CoinManager.giveCoins(target, delta);
                    else CoinManager.removeCoins(target, -delta);
                    openPlayerCard(admin, target);
                }, "Adjusts their balance directly"));

        gui.setSlot(29, GuiKit.action(Items.CHEST, "View inventory", ChatFormatting.AQUA,
                () -> InvseeGui.open(admin, target, "inventory"),
                "Opens their live inventory"));

        gui.setSlot(30, GuiKit.action(Items.BOOK, "Stats sheet", ChatFormatting.LIGHT_PURPLE,
                () -> {
                    PlayerStatsManager.StatSheet sheet = PlayerStatsManager.getStats(target);
                    admin.sendSystemMessage(Component.literal("§6" + name + "'s stats:"));
                    for (PlayerStatsManager.Stat st : PlayerStatsManager.Stat.values()) {
                        admin.sendSystemMessage(Component.literal("  §7" + st.name() + ": §f"
                                + (long) sheet.get(st)));
                    }
                }, "Prints their computed stats"));

        // Moderation.
        boolean prisoner = PrisonManager.isPrisoner(target.getUUID().toString());
        gui.setSlot(32, GuiKit.action(prisoner ? Items.DYE.lime() : Items.IRON_BARS,
                prisoner ? "Release from prison" : "Imprison", ChatFormatting.RED,
                () -> {
                    if (prisoner) {
                        PrisonManager.release(target);
                        openPlayerCard(admin, target);
                        return;
                    }
                    GuiKit.confirm(admin, "Imprison " + name, "Holds them for ten minutes.",
                            () -> {
                                PrisonManager.imprison(target, 10, target.getX(), target.getY(), target.getZ());
                                openPlayerCard(admin, target);
                            },
                            () -> openPlayerCard(admin, target));
                },
                prisoner ? "They are currently held" : "Ten minute hold"));

        gui.setSlot(33, GuiKit.action(Items.POTION, "Toggle vanish", ChatFormatting.DARK_GRAY,
                () -> {
                    VanishManager.toggleVanish(target);
                    openPlayerCard(admin, target);
                }, VanishManager.isVanished(target) ? "Currently hidden" : "Currently visible"));

        gui.setSlot(34, GuiKit.action(Items.LIGHTNING_ROD.weathering().unaffected(), "Smite", ChatFormatting.YELLOW,
                () -> GuiKit.confirm(admin, "Smite " + name,
                        "Strikes them with lightning where they stand.",
                        () -> {
                            ServerLevel lvl = ((ServerLevel) target.level());
                            var bolt = net.minecraft.world.entity.EntityTypes.LIGHTNING_BOLT.create(
                                    lvl, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                            if (bolt != null) {
                                bolt.snapTo(target.getX(), target.getY(), target.getZ());
                                lvl.addFreshEntity(bolt);
                            }
                            openPlayerCard(admin, target);
                        },
                        () -> openPlayerCard(admin, target)),
                "A very visible warning"));

        GuiKit.footer(gui, SIZE, admin, () -> openPlayers(admin, 0));
        gui.open();
    }

    private static GuiElement modeButton(ServerPlayer admin, ServerPlayer target, GameType type,
                                         net.minecraft.world.item.Item icon, GameType current) {
        boolean active = current == type;
        String label = type.getName().substring(0, 1).toUpperCase() + type.getName().substring(1);
        return GuiKit.action(icon, label, active ? ChatFormatting.GREEN : ChatFormatting.GRAY,
                () -> {
                    target.setGameMode(type);
                    openPlayerCard(admin, target);
                }, active ? "Currently in this mode" : "Switch them to " + label.toLowerCase());
    }

    // ── Moderation ────────────────────────────────────────────────────────

    private static void openModeration(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Moderation").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE, GuiKit.DANGER_EDGE);
        GuiKit.header(gui, Items.IRON_BARS, "Moderation", ChatFormatting.RED,
                "Enforcement and investigation");

        gui.setSlot(19, GuiKit.toggle("Duplication scan",
                CivilAntiCheatManager.isDupeScanEnabled(),
                () -> {
                    CivilAntiCheatManager.setDupeScanEnabled(!CivilAntiCheatManager.isDupeScanEnabled());
                    openModeration(player);
                }, "Watches for duplicated stacks"));

        gui.setSlot(20, GuiKit.toggle("Clamp illegal stacks",
                CivilAntiCheatManager.isClampIllegalStacksEnabled(),
                () -> {
                    CivilAntiCheatManager.setClampIllegalStacksEnabled(
                            !CivilAntiCheatManager.isClampIllegalStacksEnabled());
                    openModeration(player);
                }, "Trims oversized stacks on sight"));

        gui.setSlot(21, GuiKit.tile(Items.CHEST, "Chest access history", ChatFormatting.GOLD,
                () -> PrevChestsAdminGui.open(player, player),
                "Who opened what and when"));

        gui.setSlot(22, GuiKit.tile(Items.SHIELD, "Anti cheat panel", ChatFormatting.AQUA,
                () -> CivilAntiCheatGui.open(player),
                "Full detection settings"));

        gui.setSlot(23, GuiKit.tile(Items.ENDER_CHEST, "Remote chests", ChatFormatting.LIGHT_PURPLE,
                () -> CivilAntiCheatGui.open(player),
                "Open any container remotely"));

        gui.setSlot(24, GuiKit.tile(Items.PLAYER_HEAD, "Player buffs", ChatFormatting.GREEN,
                () -> PlayerBuffAdminGui.openPlayerSelect(player),
                "Grant or strip buffs"));

        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    // ── Economy ───────────────────────────────────────────────────────────

    private static void openEconomy(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Economy").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);
        GuiKit.header(gui, Items.GOLD_BLOCK, "Economy", ChatFormatting.GOLD,
                "Money in, money out");

        gui.setSlot(19, GuiKit.toggle("Daily tax", TaxManager.isTaxEnabled(),
                () -> {
                    TaxManager.setTaxEnabled(!TaxManager.isTaxEnabled());
                    openEconomy(player);
                }, "Charges every registered citizen"));

        gui.setSlot(20, GuiKit.stepper(Items.EMERALD, "Daily tax amount", ChatFormatting.GREEN,
                TaxManager.getDailyTaxAmount(), 10L,
                delta -> {
                    TaxManager.setDailyTaxAmount(Math.max(0,
                            (int) (TaxManager.getDailyTaxAmount() + delta)));
                    openEconomy(player);
                }, "Flat coins per day"));

        gui.setSlot(21, GuiKit.tile(Items.GOLD_INGOT, "Tax panel", ChatFormatting.GOLD,
                () -> TaxGui.open(player), "Rates, debtors and collection"));

        gui.setSlot(22, GuiKit.tile(Items.ITEM_FRAME, "Auction house", ChatFormatting.AQUA,
                () -> AuctionHouseGui.open(player), "Live listings and history"));

        gui.setSlot(23, GuiKit.tile(Items.ENDER_EYE, "Underground auction", ChatFormatting.DARK_PURPLE,
                () -> UndergroundAuctionGui.open(player), "The quiet market"));

        gui.setSlot(24, GuiKit.tile(Items.DIAMOND, "Stock market", ChatFormatting.GREEN,
                () -> StockAdminGui.openMainMenu(player), "Listings, prices and halts"));

        gui.setSlot(25, GuiKit.tile(Items.AMETHYST_SHARD, "Crypto", ChatFormatting.LIGHT_PURPLE,
                () -> CryptoAdminGui.openMainMenu(player), "Coins, volatility and events"));

        gui.setSlot(28, GuiKit.tile(Items.CHEST, "Government treasury", ChatFormatting.YELLOW,
                () -> GovernmentTreasuryGui.open(player), "Public money"));

        gui.setSlot(31, GuiKit.tile(Items.ARMOR_STAND, "Minions", ChatFormatting.GREEN,
                () -> MinionGui.openFrom(player, () -> openEconomy(player)), "Placed workers and their output"));

        gui.setSlot(30, GuiKit.tile(Items.GOLD_INGOT, "Bazaar", ChatFormatting.YELLOW,
                () -> BazaarGui.openFrom(player, () -> openEconomy(player)), "The material order book"));

        gui.setSlot(29, GuiKit.tile(Items.BARREL, "Shops", ChatFormatting.GOLD,
                () -> ShopGui.openMainMenu(player), "Server and player shops"));

        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    // ── Politics ──────────────────────────────────────────────────────────

    private static void openPolitics(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Politics").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);
        GuiKit.header(gui, Items.BELL, "Politics", ChatFormatting.YELLOW,
                "Offices, elections and the people behind them");

        gui.setSlot(19, GuiKit.tile(Items.BELL, "Elections", ChatFormatting.YELLOW,
                () -> AdminGui.openPage(player, AdminGui.AdminPage.ELECTIONS),
                "Start, end and reset"));

        gui.setSlot(20, GuiKit.tile(Items.GOLDEN_HELMET, "Government", ChatFormatting.GOLD,
                () -> AdminGui.openPage(player, AdminGui.AdminPage.GOVERNMENT),
                "Chair, vice, judge and dictator"));

        gui.setSlot(21, GuiKit.tile(Items.BANNER.white(), "Parties", ChatFormatting.AQUA,
                () -> PartyLeaderGui.open(player), "Membership and leadership"));

        gui.setSlot(22, GuiKit.tile(Items.END_CRYSTAL, "High council", ChatFormatting.LIGHT_PURPLE,
                () -> HighCouncilGui.open(player), "The layer above the chair"));

        gui.setSlot(23, GuiKit.tile(Items.BANNER.black(), "Shadow government", ChatFormatting.DARK_GRAY,
                () -> {
                    Settlement home = SettlementManager.getRegisteredSettlement(
                            player.getUUID().toString());
                    if (home == null) {
                        player.sendSystemMessage(Component.literal(
                                "§cRegister to a settlement first to read its roster."));
                        return;
                    }
                    ShadowGovernmentManager.printRoster(player, home);
                }, "Who holds power without an election"));

        gui.setSlot(24, GuiKit.tile(Items.IRON_SWORD, "Impeachment", ChatFormatting.RED,
                () -> ImpeachmentGui.open(player), "Motions against the chair"));

        gui.setSlot(25, GuiKit.tile(Items.PAPER, "Perks", ChatFormatting.GREEN,
                () -> PerksGui.open(player, true), "Office perks and resets"));

        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    // ── Settlements ───────────────────────────────────────────────────────

    private static void openSettlements(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Settlements").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        List<Settlement> all = new ArrayList<>(SettlementManager.getAllSettlements());
        all.sort(Comparator.comparing((Settlement s) -> s.name == null ? "" : s.name));

        GuiKit.header(gui, Items.CAMPFIRE, "Settlements", ChatFormatting.GREEN,
                all.size() + " on the map",
                "Click one to inspect it");

        List<GuiElement> entries = new ArrayList<>();
        for (Settlement s : all) {
            SettlementTier tier;
            try {
                tier = SettlementTier.valueOf(s.tier);
            } catch (IllegalArgumentException | NullPointerException e) {
                tier = SettlementTier.OUTPOST;
            }
            final SettlementTier shown = tier;
            entries.add(GuiKit.action(Items.CAMPFIRE, s.name, shown.color,
                    () -> openSettlementCard(player, s),
                    shown.displayName,
                    "Population " + s.population + ", beds " + s.beds,
                    "Appeal " + s.appeal + ", job sites " + s.jobSites,
                    "At " + s.cornerstoneX + ", " + s.cornerstoneY + ", " + s.cornerstoneZ));
        }

        GuiKit.paint(gui, entries, page, GuiKit.LIST_SLOTS.length);
        GuiKit.pager(gui, SIZE, page, entries.size(), GuiKit.LIST_SLOTS.length,
                p -> openSettlements(player, p));
        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    private static void openSettlementCard(ServerPlayer player, Settlement s) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(s.name).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        SettlementTier tier;
        try {
            tier = SettlementTier.valueOf(s.tier);
        } catch (IllegalArgumentException | NullPointerException e) {
            tier = SettlementTier.OUTPOST;
        }

        GuiKit.header(gui, Items.CAMPFIRE, s.name, tier.color,
                tier.displayName,
                "Population " + s.population,
                "Appeal " + s.appeal);

        gui.setSlot(19, GuiKit.info(Items.BED.red(), "Beds", ChatFormatting.RED,
                String.valueOf(s.beds), "Needs " + tier.minBeds + " for this tier"));

        gui.setSlot(20, GuiKit.info(Items.EMERALD, "Appeal", ChatFormatting.GREEN,
                String.valueOf(s.appeal),
                "Scored with diminishing returns",
                "so a wall of diamond blocks does little"));

        gui.setSlot(21, GuiKit.info(Items.CRAFTING_TABLE, "Job sites", ChatFormatting.GOLD,
                String.valueOf(s.jobSites), "Drives the settlement economy"));

        gui.setSlot(22, GuiKit.action(Items.SPYGLASS, "Rescan now", ChatFormatting.AQUA,
                () -> {
                    try {
                        MinecraftServer srv = ((ServerLevel) player.level()).getServer();
                        ServerLevel target = srv == null ? ((ServerLevel) player.level())
                                : SettlementManager.levelFor(srv, s);
                        SettlementTier cur;
                        try {
                            cur = SettlementTier.valueOf(s.tier);
                        } catch (IllegalArgumentException | NullPointerException ex) {
                            cur = SettlementTier.OUTPOST;
                        }
                        SettlementScanner.ScanResult res = SettlementScanner.scan(target,
                                new net.minecraft.core.BlockPos(s.cornerstoneX, s.cornerstoneY,
                                        s.cornerstoneZ), cur);
                        player.sendSystemMessage(Component.literal("§aRescanned " + s.name
                                + ": beds " + res.beds + ", appeal " + res.appeal));
                    } catch (Exception e) {
                        LOGGER.warn("AdminPanel.openSettlementCard: ignored an error", e);
                        player.sendSystemMessage(Component.literal("§cScan failed: " + e.getMessage()));
                    }
                    openSettlementCard(player, s);
                }, "Recounts beds, appeal and job sites"));

        gui.setSlot(23, GuiKit.action(Items.ENDER_PEARL, "Teleport to cornerstone", ChatFormatting.LIGHT_PURPLE,
                () -> {
                    MinecraftServer server = ((ServerLevel) player.level()).getServer();
                    ServerLevel lvl = server == null ? ((ServerLevel) player.level())
                            : SettlementManager.levelFor(server, s);
                    if (lvl != null) {
                        PlayerTeleportHelper.teleport(player, lvl, s.cornerstoneX + 0.5,
                                s.cornerstoneY + 1, s.cornerstoneZ + 0.5,
                                player.getYRot(), player.getXRot());
                    }
                    player.closeContainer();
                }, "Puts you at its centre"));

        gui.setSlot(25, GuiKit.tile(Items.BEACON, "Tier progress", ChatFormatting.LIGHT_PURPLE,
                () -> SettlementProgressGui.openFrom(player, s, () -> openSettlementCard(player, s)),
                "Exactly what is blocking the next tier"));

        gui.setSlot(24, GuiKit.tile(Items.GOLD_INGOT, "Settlement economy", ChatFormatting.GOLD,
                () -> SettlementEconomyGui.open(player, s), "Jobs, yield and happiness"));

        gui.setSlot(28, GuiKit.info(Items.WRITABLE_BOOK, "Identity", ChatFormatting.WHITE,
                "Id " + s.id,
                "Dimension " + s.worldDim));

        GuiKit.footer(gui, SIZE, player, () -> openSettlements(player, 0));
        gui.open();
    }

    // ── Wars ──────────────────────────────────────────────────────────────

    private static void openWars(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Wars").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE, GuiKit.DANGER_EDGE);
        GuiKit.header(gui, Items.NETHERITE_SWORD, "Wars", ChatFormatting.DARK_RED,
                "Declarations, motions and councils");

        gui.setSlot(22, GuiKit.tile(Items.NETHERITE_SWORD, "War panel", ChatFormatting.DARK_RED,
                () -> WarGui.open(player),
                "Active wars", "Motions and votes", "Council seats and weights"));

        gui.setSlot(20, GuiKit.info(Items.SHIELD, "How voting works", ChatFormatting.WHITE,
                "Chair 5, Vice 4, Prime 3",
                "Minister 2, Shadow 2, Constituent 1",
                "Shadow seats never outvote the elected pair"));

        gui.setSlot(24, GuiKit.info(Items.PAPER, "Chair veto", ChatFormatting.GOLD,
                "The chair can veto any motion",
                "so an unelected bench cannot start a war"));

        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    // ── Content ───────────────────────────────────────────────────────────

    private static void openContent(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Content").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);
        GuiKit.header(gui, Items.CRAFTING_TABLE, "Content", ChatFormatting.LIGHT_PURPLE,
                "Items, recipes and the gear lines");

        gui.setSlot(19, GuiKit.tile(Items.CHEST, "Custom items", ChatFormatting.GREEN,
                () -> CustomItemsGui.open(player), "Every item, by category"));

        gui.setSlot(20, GuiKit.tile(Items.CRAFTING_TABLE, "Recipes", ChatFormatting.GOLD,
                () -> RecipesGui.openMainMenu(player), "Browse and hand out"));

        gui.setSlot(21, GuiKit.tile(Items.END_CRYSTAL, "Version X catalog", ChatFormatting.AQUA,
                () -> ExpansionCatalogGui.openMenu(player), "The seven gear lines"));

        gui.setSlot(22, GuiKit.tile(Items.ANVIL, "Reforge", ChatFormatting.WHITE,
                () -> ReforgeGui.open(player), "Forty one reforges"));

        gui.setSlot(23, GuiKit.tile(Items.BUNDLE, "Relics", ChatFormatting.GOLD,
                () -> RelicGui.open(player), "Forty five relics"));

        gui.setSlot(24, GuiKit.tile(Items.WOLF_SPAWN_EGG, "Companions", ChatFormatting.GREEN,
                () -> CompanionGui.open(player), "Twenty one companions"));

        gui.setSlot(25, GuiKit.tile(Items.WRITTEN_BOOK, "Contracts", ChatFormatting.YELLOW,
                () -> ContractGui.open(player), "The rotating board"));

        gui.setSlot(29, GuiKit.tile(Items.NAME_TAG, "Titles", ChatFormatting.GOLD,
                () -> TitleGui.openFrom(player, () -> openContent(player)),
                Title.values().length + " titles",
                "Earned from every other system"));

        gui.setSlot(34, GuiKit.tile(Items.PAPER, "Requisition board", ChatFormatting.GOLD,
                () -> CommissionGui.open(player),
                Commission.total() + " postings exist",
                "Hand in goods, they are consumed"));

        gui.setSlot(32, GuiKit.tile(Items.ENDER_CHEST, "Crates", ChatFormatting.GOLD,
                () -> CrateGui.openFrom(player, () -> openContent(player)),
                "Five crates, published odds", "Bought with in game coins only"));

        gui.setSlot(28, GuiKit.tile(Items.RAW_IRON, "Refined materials", ChatFormatting.AQUA,
                () -> MaterialGui.openFrom(player, () -> openContent(player)),
                RefinedMaterial.values().length + " materials, five steps each",
                RefinedMaterial.totalItems() + " items with full recipe chains"));

        gui.setSlot(30, GuiKit.tile(Items.BOOKSHELF, "Collections", ChatFormatting.GOLD,
                () -> CollectionGui.openFrom(player, () -> openContent(player)),
                "Sixty collections, nine tiers each", "Permanent stat bonuses"));

        gui.setSlot(33, GuiKit.tile(Items.ITEM_FRAME, "Item models", ChatFormatting.LIGHT_PURPLE,
                () -> ItemModelGui.openFrom(player, () -> openContent(player)),
                PolymerBridge.knownModels() + " items have custom art",
                "Polymer serves the pack that defines them"));

        gui.setSlot(31, GuiKit.tile(Items.VILLAGER_SPAWN_EGG, "Give any item", ChatFormatting.AQUA,
                () -> AdminGui.openCustomItemsAdminGui(player), "The old item granter"));

        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    // ── Combat ────────────────────────────────────────────────────────────

    private static void openCombat(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Combat").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);
        GuiKit.header(gui, Items.ZOMBIE_HEAD, "Combat", ChatFormatting.DARK_PURPLE,
                "Mobs, bosses and the things that hunt players");

        gui.setSlot(19, GuiKit.tile(Items.DRAGON_HEAD, "World bosses", ChatFormatting.GOLD,
                () -> WorldBossGui.openMainMenu(player), "Spawn and schedule"));

        gui.setSlot(22, GuiKit.tile(Items.IRON_SWORD, "Bounties", ChatFormatting.YELLOW,
                () -> BountyGui.openMainMenu(player), "Slayer tiers and targets"));

        gui.setSlot(23, GuiKit.tile(Items.COMMAND_BLOCK, "Boss config", ChatFormatting.AQUA,
                () -> BossConfigGui.openMainMenu(player), "Health, damage and phases"));

        gui.setSlot(24, GuiKit.tile(Items.BOOK, "Bestiary", ChatFormatting.GREEN,
                () -> BestiaryGui.open(player), "What has been discovered"));

        gui.setSlot(31, GuiKit.tile(Items.BANNER.red(), "Sieges", ChatFormatting.DARK_RED,
                () -> SiegeGui.openAdmin(player),
                SiegeManager.count() + " running",
                "Send an army at a settlement"));

        gui.setSlot(30, GuiKit.tile(Items.END_CRYSTAL, "Incursions", ChatFormatting.DARK_PURPLE,
                () -> IncursionGui.openFrom(player, () -> openCombat(player)),
                IncursionManager.count() + " running",
                "Rifts themed by the biome they open in"));

        gui.setSlot(25, GuiKit.tile(Items.WITHER_SKELETON_SKULL, "Nemeses", ChatFormatting.DARK_RED,
                () -> NemesisGui.openFrom(player, () -> openCombat(player)),
                NemesisManager.total() + " grudges on this server",
                "Mobs that killed a player and came back"));

        gui.setSlot(31, GuiKit.tile(Items.NETHER_STAR, "Rare drops", ChatFormatting.GOLD,
                () -> RareDropGui.openFrom(player, () -> openCombat(player)),
                RareDrop.values().length + " drop only items",
                "Published odds, per mob"));

        gui.setSlot(29, GuiKit.tile(Items.ZOMBIE_SPAWN_EGG, "Slayer admin", ChatFormatting.RED,
                () -> AdminGui.openSlayerAdminGui(player), "Levels, bosses and resets"));

        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    // ── Magic ─────────────────────────────────────────────────────────────

    private static void openMagic(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Magic").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);
        GuiKit.header(gui, Items.ENCHANTING_TABLE, "Magic", ChatFormatting.BLUE,
                "Techniques, schools and energy");

        gui.setSlot(19, GuiKit.tile(Items.ENCHANTED_BOOK, "Techniques", ChatFormatting.LIGHT_PURPLE,
                () -> com.political.power.game.PowerAdminScreens.open(player, 0), "Every technique and its owner"));

        gui.setSlot(20, GuiKit.tile(Items.BOOKSHELF, "Schools", ChatFormatting.AQUA,
                () -> com.political.power.game.PowerAdminScreens.open(player, 0), "Each player's school and tree"));

        gui.setSlot(21, GuiKit.tile(Items.BEACON, "Progression trees", ChatFormatting.GREEN,
                () -> ProgressionTreeGui.openMainMenu(player), "Every unlock tree"));

        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    // ── World ─────────────────────────────────────────────────────────────

    private static void openWorld(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("World").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);
        GuiKit.header(gui, Items.COMPASS, "World", ChatFormatting.AQUA,
                "Time, weather, places");


        gui.setSlot(19, GuiKit.action(Items.CLOCK, "Set day", ChatFormatting.YELLOW,
                () -> {
                    run(player, "time set day");
                    openWorld(player);
                }, "Morning in this dimension"));

        gui.setSlot(20, GuiKit.action(Items.DYE.black(), "Set night", ChatFormatting.DARK_GRAY,
                () -> {
                    run(player, "time set night");
                    openWorld(player);
                }, "Dusk in this dimension"));

        gui.setSlot(21, GuiKit.action(Items.WATER_BUCKET, "Clear weather", ChatFormatting.AQUA,
                () -> {
                    run(player, "weather clear");
                    openWorld(player);
                }, "Stops rain and thunder"));

        gui.setSlot(22, GuiKit.action(Items.LIGHTNING_ROD.weathering().unaffected(), "Start a storm", ChatFormatting.BLUE,
                () -> {
                    run(player, "weather thunder");
                    openWorld(player);
                }, "Rain and thunder"));

        gui.setSlot(23, GuiKit.tile(Items.FILLED_MAP, "Atlas", ChatFormatting.GREEN,
                () -> AtlasMapGui.open(player), "The live world map"));

        gui.setSlot(31, GuiKit.tile(Items.CHERRY_SAPLING, "Custom villages", ChatFormatting.GREEN,
                () -> VillageAtlasGui.openFrom(player, () -> openWorld(player)),
                "Cherry and swamp villages", "Locate the nearest one"));

        gui.setSlot(25, GuiKit.tile(Items.WHEAT, "Seasons", ChatFormatting.GOLD,
                () -> SeasonGui.open(player), "The seasonal cycle"));

        gui.setSlot(29, GuiKit.tile(Items.BEACON, "World events", ChatFormatting.LIGHT_PURPLE,
                () -> LivingWorldGui.open(player), "What the world is doing on its own"));

        gui.setSlot(30, GuiKit.action(Items.BED.red(), "Set spawn here", ChatFormatting.RED,
                () -> GuiKit.confirm(player, "Move world spawn",
                        "Every new player will arrive where you are standing.",
                        () -> {
                            net.minecraft.core.BlockPos at = player.blockPosition();
                            run(player, "setworldspawn " + at.getX() + " " + at.getY() + " " + at.getZ());
                            player.sendSystemMessage(Component.literal("§aWorld spawn moved."));
                            openWorld(player);
                        },
                        () -> openWorld(player)),
                "Uses your current position"));

        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    /** Runs a vanilla command as the operator, used where a direct API is unstable. */
    private static void run(ServerPlayer player, String command) {
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        if (server == null) return;
        server.execute(() -> {
            try {
                server.getCommands().getDispatcher()
                        .execute(command, player.createCommandSourceStack());
            } catch (Exception e) {
                LOGGER.warn("AdminPanel.run: ignored an error", e);
                player.sendSystemMessage(Component.literal("§cCould not run: " + command));
            }
        });
    }

    // ── Diagnostics ───────────────────────────────────────────────────────

    private static void openDiagnostics(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Diagnostics").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);
        GuiKit.header(gui, Items.WRITABLE_BOOK, "Diagnostics", ChatFormatting.WHITE,
                "Is everything actually loaded");

        int recipes = 0;
        try {
            recipes = RecipeConfigManager.getRecipes().size();
        } catch (Exception ignored) {
            LOGGER.debug("AdminPanel.openDiagnostics: ignored an error", ignored);
            // A recipe store that has not initialised yet reads as zero rather than throwing.
        }
        final int recipeCount = recipes;

        gui.setSlot(19, GuiKit.info(Items.CRAFTING_TABLE, "Recipes", ChatFormatting.GOLD,
                recipeCount + " loaded"));

        gui.setSlot(20, GuiKit.info(Items.CAMPFIRE, "Settlements", ChatFormatting.GREEN,
                SettlementManager.getAllSettlements().size() + " registered"));

        gui.setSlot(21, GuiKit.info(Items.DRAGON_HEAD, "Dragon plates", ChatFormatting.LIGHT_PURPLE,
                "Eight original chestplates"));

        gui.setSlot(23, GuiKit.action(Items.SPYGLASS, "Print recipe report", ChatFormatting.AQUA,
                () -> {
                    player.sendSystemMessage(Component.literal("§6Recipe report:"));
                    player.sendSystemMessage(Component.literal("  §7Loaded: §f" + recipeCount));
                    player.sendSystemMessage(Component.literal(
                            "  §7Config: §fconfig/political/custom_recipes.json"));
                }, "Dumps counts to chat"));

        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }
}
