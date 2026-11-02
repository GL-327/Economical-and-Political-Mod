package com.political;
import com.political.government.Treasury;



import com.political.economy.CoinManager;
import com.political.government.Perk;
import com.political.recipe.RecipesGui;
import com.political.settlement.Settlement;
import com.political.settlement.SettlementManager;
import com.political.settlement.SettlementTier;
import com.political.settlement.WarGui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * V5 Pillar 2 — the complete Guilds &amp; Orders interface. Every action in the system is
 * reachable from here; {@link GuildCommands} exists only as a shortcut to this screen.
 */
public final class GuildGui {

    private GuildGui() {}

    private static final int PAGE_SIZE = 36; // slots 9..44

    // ════════════════════════════════════════════════════════════════════
    // MAIN
    // ════════════════════════════════════════════════════════════════════

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Guilds & Orders").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        String uuid = player.getUUID().toString();
        Guild g = GuildManager.getGuildOf(uuid);

        if (g == null) {
            openGuildless(gui, player, uuid);
        } else {
            openWithGuild(gui, player, uuid, g);
        }

        gui.setSlot(49, new GuiElementBuilder(Items.COMPASS)
                .setName(Component.literal("← Civilization Hub").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("§7Return to the master menu."))
                .setCallback((a, b, c, d) -> CivilizationHubGui.open(player))
                .build());

        gui.open();
    }

    private static void openGuildless(SimpleGui gui, ServerPlayer player, String uuid) {
        List<Guild> invites = GuildManager.pendingInvitesFor(uuid);

        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal(player.getName().getString())
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7You do not belong to a guild."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7A guild is a cross-settlement order:"))
                .addLoreLine(Component.literal("§7shared treasury, ranks and perks."))
                .glow().build());

        boolean hasCharter = GuildItems.hasCharter(player);
        long coins = CoinManager.getCoinsLong(player);
        boolean canAfford = coins >= GuildManager.CREATION_COST;

        GuiElementBuilder create = new GuiElementBuilder(Items.WRITTEN_BOOK)
                .setName(Component.literal("Found a Guild").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Cost: §6" + GuildManager.fmt(GuildManager.CREATION_COST) + " coins"))
                .addLoreLine(Component.literal("§7Plus: §61x Guild Charter §7(craftable)"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Charter: " + (hasCharter ? "§a✓ in inventory" : "§c✗ missing")))
                .addLoreLine(Component.literal("§7Coins: " + (canAfford ? "§a✓ " : "§c✗ ")
                        + GuildManager.fmt(coins)))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(hasCharter && canAfford
                        ? "§eClick to name your guild"
                        : "§8Requirements not met"));
        if (hasCharter && canAfford) create.glow();
        create.setCallback((a, b, c, d) -> {
            if (!GuildItems.hasCharter(player)) {
                player.sendSystemMessage(Component.literal("§cYou need a §6Guild Charter§c. §7See Recipes."));
                return;
            }
            promptCreateName(player);
        });
        gui.setSlot(20, create.build());

        GuiElementBuilder inviteBtn = new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("Pending Invitations").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Guilds that have invited you: §f" + invites.size()))
                .addLoreLine(Component.literal(invites.isEmpty() ? "§8Nothing pending" : "§eClick to review"))
                .setCallback((a, b, c, d) -> openInvitations(player));
        if (!invites.isEmpty()) inviteBtn.glow();
        gui.setSlot(22, inviteBtn.build());

        gui.setSlot(24, new GuiElementBuilder(Items.BOOKSHELF)
                .setName(Component.literal("Guild Directory").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Browse every guild on the server,"))
                .addLoreLine(Component.literal("§7their level, size and Master."))
                .setCallback((a, b, c, d) -> openDirectory(player, 0))
                .build());

        gui.setSlot(31, new GuiElementBuilder(Items.CRAFTING_TABLE)
                .setName(Component.literal("Guild Charter Recipe").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("§7Craft in a 3x3 grid:"))
                .addLoreLine(Component.literal("§8Paper  Gold   Paper"))
                .addLoreLine(Component.literal("§8Gold   Em.Blk Gold"))
                .addLoreLine(Component.literal("§8Paper  Ink    Paper"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to open the recipe browser"))
                .setCallback((a, b, c, d) -> RecipesGui.openMainMenu(player))
                .build());
    }

    private static void openWithGuild(SimpleGui gui, ServerPlayer player, String uuid, Guild g) {
        Guild.Rank rank = g.rankOf(uuid);
        if (rank == null) rank = Guild.Rank.INITIATE;

        Settlement hall = g.hallSettlementId == null ? null : SettlementManager.get(g.hallSettlementId);

        gui.setSlot(4, new GuiElementBuilder(Items.WHITE_BANNER)
                .setName(Component.literal(g.name).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§8[" + g.tag + "] §7" + (g.motto == null ? "" : g.motto)))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Level: §b" + g.level + "§8/" + GuildManager.MAX_LEVEL))
                .addLoreLine(Component.literal("§7XP: §f" + GuildManager.fmt(g.xp)
                        + (g.level >= GuildManager.MAX_LEVEL ? " §8(max)"
                        : " §8(" + GuildManager.fmt(GuildManager.xpToNextLevel(g)) + " to next)")))
                .addLoreLine(Component.literal("§7" + progressBar(GuildManager.levelProgress(g))))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Members: §f" + g.memberCount() + "§8/" + Guild.MAX_MEMBERS))
                .addLoreLine(Component.literal("§7Treasury: §6" + GuildManager.fmt(g.treasury) + " coins"))
                .addLoreLine(Component.literal("§7Hall: §f" + (hall == null ? "§8none" : hall.name)))
                .addLoreLine(Component.literal("§7Your rank: " + rank.colored()))
                .glow().build());

        gui.setSlot(10, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("Roster").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Every member and their rank."))
                .addLoreLine(Component.literal("§7Click a member to manage them."))
                .addLoreLine(Component.literal("§eClick to open"))
                .setCallback((a, b, c, d) -> openRoster(player, 0))
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("Treasury").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Balance: §6" + GuildManager.fmt(g.treasury) + " coins"))
                .addLoreLine(Component.literal("§7Deposit is open to all members."))
                .addLoreLine(Component.literal("§7Withdrawal requires Officer+."))
                .addLoreLine(Component.literal("§eClick to open"))
                .setCallback((a, b, c, d) -> openTreasury(player))
                .build());

        int owned = g.perkSet().size();
        gui.setSlot(14, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Guild Perks").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Unlocked: §f" + owned + "§8/" + GuildPerks.Perk.values().length))
                .addLoreLine(Component.literal("§7Passive bonuses for every member,"))
                .addLoreLine(Component.literal("§7bought with the guild treasury."))
                .addLoreLine(Component.literal("§eClick to open"))
                .setCallback((a, b, c, d) -> openPerks(player))
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("Activity Log").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Entries: §f" + g.logList().size()))
                .addLoreLine(Component.literal("§7Joins, ranks, coins, perks, halls."))
                .addLoreLine(Component.literal("§eClick to open"))
                .setCallback((a, b, c, d) -> openLog(player, 0))
                .build());

        gui.setSlot(20, new GuiElementBuilder(Items.BELL)
                .setName(Component.literal("Guild Hall").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Current: §f" + (hall == null ? "§8none declared" : hall.name)))
                .addLoreLine(Component.literal("§7Declare at a §fTown+ §7settlement to"))
                .addLoreLine(Component.literal("§7give it a happiness bonus."))
                .addLoreLine(Component.literal("§eClick to open"))
                .setCallback((a, b, c, d) -> openHall(player))
                .build());

        boolean canInvite = rank.isAtLeast(Guild.Rank.OFFICER);
        gui.setSlot(22, new GuiElementBuilder(canInvite ? Items.WRITABLE_BOOK : Items.BARRIER)
                .setName(Component.literal("Recruit").withStyle(
                        canInvite ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Invite an online, guildless player."))
                .addLoreLine(Component.literal("§7Pending invites: §f" + g.inviteSet().size()))
                .addLoreLine(Component.literal(canInvite ? "§eClick to open" : "§cOfficer+ only"))
                .setCallback((a, b, c, d) -> {
                    if (!canInvite) {
                        player.sendSystemMessage(Component.literal("§cOnly Officers and the Master may invite."));
                        return;
                    }
                    openRecruit(player, 0);
                })
                .build());

        boolean waystone = GuildPerks.hasWaystone(uuid) && hall != null;
        long cd = GuildManager.waystoneCooldownRemaining(uuid);
        GuiElementBuilder way = new GuiElementBuilder(waystone ? Items.ENDER_EYE : Items.ENDER_PEARL)
                .setName(Component.literal("Guild Waystone").withStyle(
                        waystone ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Travel to the guild hall."))
                .addLoreLine(Component.literal(!GuildPerks.hasWaystone(uuid)
                        ? "§cRequires the Guild Waystone perk."
                        : hall == null ? "§cYour guild has no hall."
                        : cd > 0 ? "§cRecharging: §f" + (cd / 1000L) + "s"
                        : "§eClick to travel"));
        if (waystone && cd == 0) way.glow();
        gui.setSlot(24, way.setCallback((a, b, c, d) -> {
            if (GuildManager.useWaystone(player)) gui.close();
        }).build());

        gui.setSlot(30, new GuiElementBuilder(Items.BOOKSHELF)
                .setName(Component.literal("Guild Directory").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Every guild on the server."))
                .setCallback((a, b, c, d) -> openDirectory(player, 0))
                .build());

        gui.setSlot(32, new GuiElementBuilder(Items.COMPARATOR)
                .setName(Component.literal("Guild Settings").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Name, tag, motto, disband."))
                .setCallback((a, b, c, d) -> openSettings(player))
                .build());

        gui.setSlot(40, new GuiElementBuilder(Items.OAK_DOOR)
                .setName(Component.literal("Leave Guild").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Give up your rank and perks."))
                .addLoreLine(Component.literal(rank == Guild.Rank.MASTER && g.memberCount() > 1
                        ? "§cTransfer mastery first."
                        : "§cClick to leave"))
                .setCallback((a, b, c, d) -> {
                    if (GuildManager.leave(player)) open(player);
                })
                .build());

        gui.setSlot(45, new GuiElementBuilder(Items.CLOCK)
                .setName(Component.literal("Your Daily Contribution").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("§7Guild XP you have banked today."))
                .addLoreLine(Component.literal("§7Caps stop AFK farms paying out."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Kills: §f" + GuildManager.dailyKillXp(uuid)
                        + "§8/" + GuildManager.DAILY_KILL_XP_CAP))
                .addLoreLine(Component.literal("§7Crafting: §f" + GuildManager.dailyCraftXp(uuid)
                        + "§8/" + GuildManager.DAILY_CRAFT_XP_CAP))
                .addLoreLine(Component.literal("§7Expeditions: §f" + GuildManager.dailyExpeditionXp(uuid)
                        + "§8/" + GuildManager.DAILY_EXPEDITION_XP_CAP))
                .build());
    }

    // ════════════════════════════════════════════════════════════════════
    // ROSTER
    // ════════════════════════════════════════════════════════════════════

    public static void openRoster(ServerPlayer player, int page) {
        Guild g = GuildManager.getGuildOf(player);
        if (g == null) { open(player); return; }

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Roster — " + g.name).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        List<String> roster = g.rosterSorted();
        int pages = Math.max(1, (roster.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int p = Math.max(0, Math.min(page, pages - 1));

        for (int i = 0; i < PAGE_SIZE; i++) {
            int idx = p * PAGE_SIZE + i;
            if (idx >= roster.size()) break;
            String memberUuid = roster.get(idx);
            Guild.Rank r = g.rankOf(memberUuid);
            String name = DataManager.getPlayerName(memberUuid);
            boolean online = PoliticalServer.server != null
                    && PoliticalServer.server.getPlayerList().getPlayer(java.util.UUID.fromString(memberUuid)) != null;

            gui.setSlot(9 + i, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setName(Component.literal((r == null ? "§7" : r.colorCode) + name)
                            .withStyle(ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Rank: " + (r == null ? "§8?" : r.colored())))
                    .addLoreLine(Component.literal("§7Status: " + (online ? "§aonline" : "§8offline")))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to manage"))
                    .setCallback((a, b, c, d) -> openMember(player, memberUuid))
                    .build());
        }

        pager(gui, p, pages, np -> openRoster(player, np));
        gui.setSlot(49, WarGui.back(() -> open(player)));
        gui.open();
    }

    public static void openMember(ServerPlayer player, String targetUuid) {
        Guild g = GuildManager.getGuildOf(player);
        if (g == null || targetUuid == null || !g.isMember(targetUuid)) { openRoster(player, 0); return; }

        String actorUuid = player.getUUID().toString();
        Guild.Rank actorRank = g.rankOf(actorUuid);
        Guild.Rank targetRank = g.rankOf(targetUuid);
        if (actorRank == null || targetRank == null) { openRoster(player, 0); return; }
        String name = DataManager.getPlayerName(targetUuid);

        boolean isSelf = actorUuid.equals(targetUuid);
        boolean officer = actorRank.isAtLeast(Guild.Rank.OFFICER);
        boolean outranks = actorRank.weight > targetRank.weight;
        boolean canManage = officer && outranks && !isSelf;

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal(name).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        WarGui.fill(gui, 27);

        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal(name).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Rank: " + targetRank.colored()))
                .addLoreLine(Component.literal("§7Guild: §f" + g.name))
                .glow().build());

        gui.setSlot(11, new GuiElementBuilder(canManage ? Items.LIME_DYE : Items.GRAY_DYE)
                .setName(Component.literal("Promote").withStyle(
                        canManage ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7→ " + targetRank.up().colored()))
                .addLoreLine(Component.literal(canManage ? "§eClick to promote" : "§cNot permitted"))
                .setCallback((a, b, c, d) -> {
                    if (GuildManager.promote(player, targetUuid)) openMember(player, targetUuid);
                })
                .build());

        gui.setSlot(13, new GuiElementBuilder(canManage ? Items.ORANGE_DYE : Items.GRAY_DYE)
                .setName(Component.literal("Demote").withStyle(
                        canManage ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7→ " + targetRank.down().colored()))
                .addLoreLine(Component.literal(canManage ? "§eClick to demote" : "§cNot permitted"))
                .setCallback((a, b, c, d) -> {
                    if (GuildManager.demote(player, targetUuid)) openMember(player, targetUuid);
                })
                .build());

        gui.setSlot(15, new GuiElementBuilder(canManage ? Items.BARRIER : Items.GRAY_DYE)
                .setName(Component.literal("Remove from Guild").withStyle(
                        canManage ? ChatFormatting.RED : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(canManage ? "§cClick to kick" : "§cNot permitted"))
                .setCallback((a, b, c, d) -> {
                    if (GuildManager.kick(player, targetUuid)) openRoster(player, 0);
                })
                .build());

        boolean canTransfer = actorRank == Guild.Rank.MASTER && !isSelf;
        gui.setSlot(17, new GuiElementBuilder(canTransfer ? Items.GOLDEN_HELMET : Items.GRAY_DYE)
                .setName(Component.literal("Transfer Mastery").withStyle(
                        canTransfer ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Make them Guild Master."))
                .addLoreLine(Component.literal("§7You become an Officer."))
                .addLoreLine(Component.literal(canTransfer ? "§cThis cannot be undone" : "§cMaster only"))
                .setCallback((a, b, c, d) -> {
                    if (GuildManager.transferMastery(player, targetUuid)) openRoster(player, 0);
                })
                .build());

        gui.setSlot(22, WarGui.back(() -> openRoster(player, 0)));
        gui.open();
    }

    // ════════════════════════════════════════════════════════════════════
    // TREASURY
    // ════════════════════════════════════════════════════════════════════

    public static void openTreasury(ServerPlayer player) {
        Guild g = GuildManager.getGuildOf(player);
        if (g == null) { open(player); return; }
        String uuid = player.getUUID().toString();
        Guild.Rank rank = g.rankOf(uuid);
        if (rank == null) rank = Guild.Rank.INITIATE;
        boolean officer = rank.isAtLeast(Guild.Rank.OFFICER);

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Guild Treasury").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("Treasury").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Guild balance: §6" + GuildManager.fmt(g.treasury)))
                .addLoreLine(Component.literal("§7Your coins: §e" + GuildManager.fmt(CoinManager.getCoinsLong(player))))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Withdrawable today: §f"
                        + GuildManager.fmt(GuildManager.withdrawalHeadroom(g, uuid))))
                .glow().build());

        long[] amounts = {1_000L, 10_000L, 100_000L, 1_000_000L};
        int[] depositSlots = {19, 20, 21, 22};
        for (int i = 0; i < amounts.length; i++) {
            long amt = amounts[i];
            gui.setSlot(depositSlots[i], new GuiElementBuilder(Items.LIME_CONCRETE)
                    .setName(Component.literal("Deposit " + GuildManager.fmt(amt))
                            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Move coins from you to the guild."))
                    .addLoreLine(Component.literal("§8Deposits award no guild XP."))
                    .addLoreLine(Component.literal("§eClick to deposit"))
                    .setCallback((a, b, c, d) -> {
                        GuildManager.deposit(player, amt);
                        openTreasury(player);
                    })
                    .build());
        }

        gui.setSlot(23, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("Deposit Custom Amount").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§eClick to type an amount"))
                .setCallback((a, b, c, d) -> promptAmount(player, "Deposit", amt -> {
                    GuildManager.deposit(player, amt);
                    openTreasury(player);
                }))
                .build());

        int[] withdrawSlots = {28, 29, 30, 31};
        for (int i = 0; i < amounts.length; i++) {
            long amt = amounts[i];
            final boolean allowed = officer;
            gui.setSlot(withdrawSlots[i], new GuiElementBuilder(allowed ? Items.RED_CONCRETE : Items.GRAY_CONCRETE)
                    .setName(Component.literal("Withdraw " + GuildManager.fmt(amt))
                            .withStyle(allowed ? ChatFormatting.RED : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Move coins from the guild to you."))
                    .addLoreLine(Component.literal(allowed
                            ? "§8Officers are capped at "
                              + GuildManager.fmt(GuildManager.OFFICER_DAILY_WITHDRAW_CAP) + "/day"
                            : "§cOfficer+ only"))
                    .setCallback((a, b, c, d) -> {
                        if (!allowed) {
                            player.sendSystemMessage(Component.literal("§cOnly Officers and the Master may withdraw."));
                            return;
                        }
                        GuildManager.withdraw(player, amt);
                        openTreasury(player);
                    })
                    .build());
        }

        final boolean allowedCustom = officer;
        gui.setSlot(32, new GuiElementBuilder(allowedCustom ? Items.PAPER : Items.GRAY_DYE)
                .setName(Component.literal("Withdraw Custom Amount")
                        .withStyle(allowedCustom ? ChatFormatting.RED : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(allowedCustom ? "§eClick to type an amount" : "§cOfficer+ only"))
                .setCallback((a, b, c, d) -> {
                    if (!allowedCustom) return;
                    promptAmount(player, "Withdraw", amt -> {
                        GuildManager.withdraw(player, amt);
                        openTreasury(player);
                    });
                })
                .build());

        Settlement hall = g.hallSettlementId == null ? null : SettlementManager.get(g.hallSettlementId);
        boolean canPatron = officer && hall != null;
        gui.setSlot(40, new GuiElementBuilder(canPatron ? Items.EMERALD : Items.GRAY_DYE)
                .setName(Component.literal("Fund the Hall").withStyle(
                        canPatron ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Transfer guild coins into your hall"))
                .addLoreLine(Component.literal("§7settlement's treasury."))
                .addLoreLine(Component.literal("§7Hall: §f" + (hall == null ? "§8none" : hall.name)))
                .addLoreLine(Component.literal(canPatron ? "§eClick to send an amount" : "§cNeeds a hall and Officer+"))
                .setCallback((a, b, c, d) -> {
                    if (!canPatron) return;
                    promptAmount(player, "Patronage", amt -> {
                        GuildManager.patronage(player, amt);
                        openTreasury(player);
                    });
                })
                .build());

        gui.setSlot(49, WarGui.back(() -> open(player)));
        gui.open();
    }

    // ════════════════════════════════════════════════════════════════════
    // PERKS
    // ════════════════════════════════════════════════════════════════════

    public static void openPerks(ServerPlayer player) {
        Guild g = GuildManager.getGuildOf(player);
        if (g == null) { open(player); return; }
        boolean isMaster = g.rankOf(player.getUUID().toString()) == Guild.Rank.MASTER;

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Guild Perks").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal(g.name + " — Level " + g.level)
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Treasury: §6" + GuildManager.fmt(g.treasury) + " coins"))
                .addLoreLine(Component.literal("§7" + progressBar(GuildManager.levelProgress(g))))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(isMaster
                        ? "§7Only you (the Master) may purchase."
                        : "§8Only the Guild Master may purchase."))
                .glow().build());

        int[] slots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        GuildPerks.Perk[] perks = GuildPerks.Perk.values();
        for (int i = 0; i < perks.length && i < slots.length; i++) {
            GuildPerks.Perk perk = perks[i];
            boolean owned = g.hasPerk(perk.id);
            boolean available = GuildPerks.isAvailable(g, perk);
            boolean affordable = g.treasury >= perk.cost;

            GuiElementBuilder b = new GuiElementBuilder(owned ? perk.icon
                    : available ? perk.icon : Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal((owned ? "§a✔ " : available ? "§e" : "§8") + perk.display)
                            .withStyle(ChatFormatting.BOLD));
            for (String line : perk.lore) {
                b.addLoreLine(Component.literal(line));
            }
            b.addLoreLine(Component.literal(""));
            b.addLoreLine(Component.literal("§7Cost: §6" + GuildManager.fmt(perk.cost) + " coins"));
            b.addLoreLine(Component.literal("§7Requires level: §f" + perk.requiredLevel));
            if (perk.requires != null) {
                GuildPerks.Perk req = GuildPerks.Perk.byId(perk.requires);
                b.addLoreLine(Component.literal("§7Requires perk: §f" + (req == null ? perk.requires : req.display)));
            }
            b.addLoreLine(Component.literal(""));
            if (owned) {
                b.addLoreLine(Component.literal("§aUnlocked"));
                b.glow();
            } else if (!available) {
                b.addLoreLine(Component.literal(GuildPerks.blockedReason(g, perk)));
            } else if (!affordable) {
                b.addLoreLine(Component.literal("§cTreasury short by §f"
                        + GuildManager.fmt(perk.cost - g.treasury)));
            } else if (!isMaster) {
                b.addLoreLine(Component.literal("§cGuild Master only"));
            } else {
                b.addLoreLine(Component.literal("§eClick to purchase"));
                b.glow();
            }

            b.setCallback((a, c, d, e) -> {
                if (GuildManager.purchasePerk(player, perk)) openPerks(player);
            });
            gui.setSlot(slots[i], b.build());
        }

        gui.setSlot(49, WarGui.back(() -> open(player)));
        gui.open();
    }

    // ════════════════════════════════════════════════════════════════════
    // LOG
    // ════════════════════════════════════════════════════════════════════

    public static void openLog(ServerPlayer player, int page) {
        Guild g = GuildManager.getGuildOf(player);
        if (g == null) { open(player); return; }

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Guild Log").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        List<String> entries = new ArrayList<>(g.logList());
        java.util.Collections.reverse(entries); // newest first
        int pages = Math.max(1, (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int p = Math.max(0, Math.min(page, pages - 1));

        for (int i = 0; i < PAGE_SIZE; i++) {
            int idx = p * PAGE_SIZE + i;
            if (idx >= entries.size()) break;
            gui.setSlot(9 + i, new GuiElementBuilder(Items.PAPER)
                    .setName(Component.literal(entries.get(idx)))
                    .build());
        }
        if (entries.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("§8No activity yet"))
                    .build());
        }

        pager(gui, p, pages, np -> openLog(player, np));
        gui.setSlot(49, WarGui.back(() -> open(player)));
        gui.open();
    }

    // ════════════════════════════════════════════════════════════════════
    // RECRUIT / INVITATIONS
    // ════════════════════════════════════════════════════════════════════

    public static void openRecruit(ServerPlayer player, int page) {
        Guild g = GuildManager.getGuildOf(player);
        if (g == null) { open(player); return; }

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Recruit").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        List<ServerPlayer> candidates = new ArrayList<>();
        if (PoliticalServer.server != null) {
            for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
                if (GuildManager.getGuildOf(p) == null) candidates.add(p);
            }
        }

        int pages = Math.max(1, (candidates.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int pg = Math.max(0, Math.min(page, pages - 1));

        for (int i = 0; i < PAGE_SIZE; i++) {
            int idx = pg * PAGE_SIZE + i;
            if (idx >= candidates.size()) break;
            ServerPlayer target = candidates.get(idx);
            boolean invited = g.inviteSet().contains(target.getUUID().toString());
            gui.setSlot(9 + i, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setName(Component.literal(target.getName().getString())
                            .withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Guildless."))
                    .addLoreLine(Component.literal(invited ? "§aInvite already sent" : "§eClick to invite"))
                    .setCallback((a, b, c, d) -> {
                        GuildManager.invite(player, target);
                        openRecruit(player, pg);
                    })
                    .build());
        }
        if (candidates.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("§8No guildless players are online"))
                    .build());
        }

        pager(gui, pg, pages, np -> openRecruit(player, np));
        gui.setSlot(49, WarGui.back(() -> open(player)));
        gui.open();
    }

    public static void openInvitations(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        List<Guild> invites = GuildManager.pendingInvitesFor(uuid);

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Invitations").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        int slot = 10;
        for (Guild g : invites) {
            if (slot > 43) break;
            gui.setSlot(slot, new GuiElementBuilder(Items.WHITE_BANNER)
                    .setName(Component.literal(g.name).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§8[" + g.tag + "] §7" + (g.motto == null ? "" : g.motto)))
                    .addLoreLine(Component.literal("§7Level §b" + g.level + " §7• §f" + g.memberCount() + " members"))
                    .addLoreLine(Component.literal("§7Master: §f" + DataManager.getPlayerName(g.masterUuid())))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§aLeft-click to accept"))
                    .addLoreLine(Component.literal("§cRight-click to decline"))
                    .setCallback((index, clickType, action, slotGui) -> {
                        if (clickType.isRight) {
                            GuildManager.declineInvite(player, g.id);
                        } else {
                            GuildManager.acceptInvite(player, g.id);
                        }
                        openInvitations(player);
                    })
                    .build());
            slot++;
            if (slot % 9 == 8) slot += 2;
        }
        if (invites.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("§8No pending invitations"))
                    .build());
        }

        gui.setSlot(49, WarGui.back(() -> open(player)));
        gui.open();
    }

    // ════════════════════════════════════════════════════════════════════
    // HALL
    // ════════════════════════════════════════════════════════════════════

    public static void openHall(ServerPlayer player) {
        Guild g = GuildManager.getGuildOf(player);
        if (g == null) { open(player); return; }
        boolean isMaster = g.rankOf(player.getUUID().toString()) == Guild.Rank.MASTER;

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Guild Hall").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        Settlement hall = g.hallSettlementId == null ? null : SettlementManager.get(g.hallSettlementId);
        Settlement here = CivilizationHubGui.currentOrHome(player);

        int residents = 0;
        if (hall != null) {
            for (String u : g.memberMap().keySet()) {
                if (hall.registered().contains(u)) residents++;
            }
        }

        gui.setSlot(4, new GuiElementBuilder(Items.BELL)
                .setName(Component.literal("Current Hall").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Settlement: §f" + (hall == null ? "§8none declared" : hall.name)))
                .addLoreLine(Component.literal(hall == null ? "" : "§7Tier: " + hall.tierEnum().colored()))
                .addLoreLine(Component.literal(hall == null ? "" : "§7Resident members: §f" + residents))
                .addLoreLine(Component.literal(hall == null ? "" : "§7Happiness bonus: §a+"
                        + GuildPerks.hallHappinessBonus(hall.id)))
                .glow().build());

        boolean canDeclare = isMaster && here != null
                && here.tierEnum().isAtLeast(SettlementTier.TOWN)
                && (hall == null || !here.id.equals(hall.id));

        gui.setSlot(20, new GuiElementBuilder(canDeclare ? Items.LIME_CONCRETE : Items.GRAY_CONCRETE)
                .setName(Component.literal("Declare Hall Here").withStyle(
                        canDeclare ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Settlement: §f"
                        + (here == null ? "§8none nearby" : here.name)))
                .addLoreLine(Component.literal("§7Requires: §fTown+§7, §f"
                        + GuildManager.HALL_MIN_RESIDENT_MEMBERS + " §7resident members"))
                .addLoreLine(Component.literal("§7Cost: §6" + GuildManager.fmt(GuildManager.HALL_DECLARE_COST)
                        + " coins §7from the treasury"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(isMaster
                        ? (canDeclare ? "§eClick to declare" : "§cRequirements not met here")
                        : "§cGuild Master only"))
                .setCallback((a, b, c, d) -> {
                    Settlement s = CivilizationHubGui.currentOrHome(player);
                    if (s == null) {
                        player.sendSystemMessage(Component.literal("§cStand in a settlement first."));
                        return;
                    }
                    if (GuildManager.declareHall(player, s)) openHall(player);
                })
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("What a Hall Does").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Guild members registered to the hall"))
                .addLoreLine(Component.literal("§7settlement raise its §ahappiness§7,"))
                .addLoreLine(Component.literal("§7scaling with how many live there."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Hall Charter perk: §aup to +4"))
                .addLoreLine(Component.literal("§7Grand Hall perk: §aup to +8§7, and"))
                .addLoreLine(Component.literal("§7resident members earn §f+10% §7guild XP."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§8The charter lapses if the settlement"))
                .addLoreLine(Component.literal("§8falls below Town."))
                .build());

        boolean canAbandon = isMaster && hall != null;
        gui.setSlot(24, new GuiElementBuilder(canAbandon ? Items.BARRIER : Items.GRAY_DYE)
                .setName(Component.literal("Abandon Hall").withStyle(
                        canAbandon ? ChatFormatting.RED : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7The declaration fee is not refunded."))
                .addLoreLine(Component.literal(canAbandon ? "§cClick to abandon" : "§cNothing to abandon"))
                .setCallback((a, b, c, d) -> {
                    if (GuildManager.abandonHall(player)) openHall(player);
                })
                .build());

        gui.setSlot(49, WarGui.back(() -> open(player)));
        gui.open();
    }

    // ════════════════════════════════════════════════════════════════════
    // DIRECTORY
    // ════════════════════════════════════════════════════════════════════

    public static void openDirectory(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Guild Directory").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        List<Guild> all = GuildManager.leaderboard();
        int pages = Math.max(1, (all.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int p = Math.max(0, Math.min(page, pages - 1));

        for (int i = 0; i < PAGE_SIZE; i++) {
            int idx = p * PAGE_SIZE + i;
            if (idx >= all.size()) break;
            Guild g = all.get(idx);
            Settlement hall = g.hallSettlementId == null ? null : SettlementManager.get(g.hallSettlementId);
            gui.setSlot(9 + i, new GuiElementBuilder(Items.WHITE_BANNER)
                    .setName(Component.literal("#" + (idx + 1) + " " + g.name)
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§8[" + g.tag + "] §7" + (g.motto == null ? "" : g.motto)))
                    .addLoreLine(Component.literal("§7Level: §b" + g.level))
                    .addLoreLine(Component.literal("§7Members: §f" + g.memberCount()))
                    .addLoreLine(Component.literal("§7Master: §f" + DataManager.getPlayerName(g.masterUuid())))
                    .addLoreLine(Component.literal("§7Hall: §f" + (hall == null ? "§8none" : hall.name)))
                    .addLoreLine(Component.literal("§7Perks: §f" + g.perkSet().size()))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§8Guilds are invite-only — speak to the Master."))
                    .build());
        }
        if (all.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("§8No guilds have been chartered yet"))
                    .build());
        }

        pager(gui, p, pages, np -> openDirectory(player, np));
        gui.setSlot(49, WarGui.back(() -> open(player)));
        gui.open();
    }

    // ════════════════════════════════════════════════════════════════════
    // SETTINGS
    // ════════════════════════════════════════════════════════════════════

    public static void openSettings(ServerPlayer player) {
        Guild g = GuildManager.getGuildOf(player);
        if (g == null) { open(player); return; }
        String uuid = player.getUUID().toString();
        Guild.Rank rank = g.rankOf(uuid);
        if (rank == null) rank = Guild.Rank.INITIATE;
        boolean isMaster = rank == Guild.Rank.MASTER;
        boolean officer = rank.isAtLeast(Guild.Rank.OFFICER);

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Guild Settings").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        gui.setSlot(4, new GuiElementBuilder(Items.NAME_TAG)
                .setName(Component.literal(g.name).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§8[" + g.tag + "]"))
                .addLoreLine(Component.literal("§7" + (g.motto == null || g.motto.isEmpty() ? "§8no motto" : g.motto)))
                .glow().build());

        gui.setSlot(20, new GuiElementBuilder(isMaster ? Items.NAME_TAG : Items.GRAY_DYE)
                .setName(Component.literal("Rename Guild").withStyle(
                        isMaster ? ChatFormatting.YELLOW : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Cost: §6" + GuildManager.fmt(GuildManager.RENAME_COST)
                        + " coins §7from the treasury"))
                .addLoreLine(Component.literal(isMaster ? "§eClick to rename" : "§cGuild Master only"))
                .setCallback((a, b, c, d) -> {
                    if (!isMaster) return;
                    promptRenameName(player);
                })
                .build());

        final boolean canMotto = officer;
        gui.setSlot(22, new GuiElementBuilder(canMotto ? Items.WRITABLE_BOOK : Items.GRAY_DYE)
                .setName(Component.literal("Set Motto").withStyle(
                        canMotto ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Shown in the directory and roster."))
                .addLoreLine(Component.literal(canMotto ? "§eClick to set" : "§cOfficer+ only"))
                .setCallback((a, b, c, d) -> {
                    if (!canMotto) return;
                    SignGui sign = new SignGui(player) {
                        @Override
                        public void onManualClose() {
                            String line = this.getLine(0).getString().trim();
                            GuildManager.setMotto(player, line);
                            openSettings(player);
                        }
                    };
                    sign.setLine(1, Component.literal("^ Guild motto"));
                    sign.setLine(2, Component.literal("Max 48 chars"));
                    sign.open();
                })
                .build());

        gui.setSlot(24, new GuiElementBuilder(isMaster ? Items.TNT : Items.GRAY_DYE)
                .setName(Component.literal("Disband Guild").withStyle(
                        isMaster ? ChatFormatting.DARK_RED : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§cPermanently destroys the guild."))
                .addLoreLine(Component.literal("§cThe treasury is forfeit."))
                .addLoreLine(Component.literal(isMaster ? "§4Click to confirm on the next screen" : "§cGuild Master only"))
                .setCallback((a, b, c, d) -> {
                    if (!isMaster) return;
                    confirmDisband(player);
                })
                .build());

        gui.setSlot(49, WarGui.back(() -> open(player)));
        gui.open();
    }

    private static void confirmDisband(ServerPlayer player) {
        Guild g = GuildManager.getGuildOf(player);
        if (g == null) { open(player); return; }

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("Disband " + g.name + "?").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        WarGui.fill(gui, 27);

        gui.setSlot(11, new GuiElementBuilder(Items.RED_CONCRETE)
                .setName(Component.literal("YES — disband").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§cThis cannot be undone."))
                .addLoreLine(Component.literal("§c" + GuildManager.fmt(g.treasury) + " coins will be lost."))
                .setCallback((a, b, c, d) -> {
                    GuildManager.disband(player, g);
                    open(player);
                })
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.LIME_CONCRETE)
                .setName(Component.literal("NO — keep the guild").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .setCallback((a, b, c, d) -> openSettings(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════════════
    // TEXT PROMPTS
    // ════════════════════════════════════════════════════════════════════

    private static void promptCreateName(ServerPlayer player) {
        SignGui sign = new SignGui(player) {
            @Override
            public void onManualClose() {
                String name = GuildManager.sanitizeName(this.getLine(0).getString());
                if (name.length() < 3) {
                    player.sendSystemMessage(Component.literal("§cGuild names must be at least 3 characters."));
                    GuildGui.open(player);
                    return;
                }
                promptCreateTag(player, name);
            }
        };
        sign.setLine(1, Component.literal("^ Guild name"));
        sign.setLine(2, Component.literal("3-24 characters"));
        sign.open();
    }

    private static void promptCreateTag(ServerPlayer player, String name) {
        SignGui sign = new SignGui(player) {
            @Override
            public void onManualClose() {
                String tag = GuildManager.sanitizeTag(this.getLine(0).getString());
                if (tag.length() < 2) {
                    player.sendSystemMessage(Component.literal("§cGuild tags must be 2-5 characters."));
                    GuildGui.open(player);
                    return;
                }
                GuildManager.createGuild(player, name, tag);
                GuildGui.open(player);
            }
        };
        sign.setLine(1, Component.literal("^ Guild tag"));
        sign.setLine(2, Component.literal("2-5 chars, e.g."));
        sign.setLine(3, Component.literal("IRON"));
        sign.open();
    }

    private static void promptRenameName(ServerPlayer player) {
        SignGui sign = new SignGui(player) {
            @Override
            public void onManualClose() {
                String name = GuildManager.sanitizeName(this.getLine(0).getString());
                if (name.length() < 3) {
                    player.sendSystemMessage(Component.literal("§cGuild names must be at least 3 characters."));
                    openSettings(player);
                    return;
                }
                promptRenameTag(player, name);
            }
        };
        sign.setLine(1, Component.literal("^ New guild name"));
        sign.setLine(2, Component.literal("3-24 characters"));
        sign.open();
    }

    private static void promptRenameTag(ServerPlayer player, String name) {
        SignGui sign = new SignGui(player) {
            @Override
            public void onManualClose() {
                String tag = GuildManager.sanitizeTag(this.getLine(0).getString());
                if (tag.length() < 2) {
                    player.sendSystemMessage(Component.literal("§cGuild tags must be 2-5 characters."));
                    openSettings(player);
                    return;
                }
                GuildManager.rename(player, name, tag);
                openSettings(player);
            }
        };
        sign.setLine(1, Component.literal("^ New guild tag"));
        sign.setLine(2, Component.literal("2-5 characters"));
        sign.open();
    }

    /** Callback target for the numeric sign prompt. */
    private interface AmountConsumer {
        void accept(long amount);
    }

    private static void promptAmount(ServerPlayer player, String label, AmountConsumer consumer) {
        SignGui sign = new SignGui(player) {
            @Override
            public void onManualClose() {
                String raw = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (raw.isEmpty()) {
                    openTreasury(player);
                    return;
                }
                try {
                    long amount = Long.parseLong(raw);
                    if (amount <= 0) {
                        player.sendSystemMessage(Component.literal("§cAmount must be positive."));
                        openTreasury(player);
                        return;
                    }
                    consumer.accept(amount);
                } catch (NumberFormatException e) {
                    player.sendSystemMessage(Component.literal("§cThat is not a valid number."));
                    openTreasury(player);
                }
            }
        };
        sign.setLine(1, Component.literal("^ " + label));
        sign.setLine(2, Component.literal("Coins, digits only"));
        sign.open();
    }

    // ════════════════════════════════════════════════════════════════════
    // SHARED WIDGETS
    // ════════════════════════════════════════════════════════════════════

    private interface PageOpener {
        void open(int page);
    }

    private static void pager(SimpleGui gui, int page, int pages, PageOpener opener) {
        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((a, b, c, d) -> opener.open(page - 1))
                    .build());
        }
        if (page < pages - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .setCallback((a, b, c, d) -> opener.open(page + 1))
                    .build());
        }
        gui.setSlot(48, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("§7Page §f" + (page + 1) + "§8/§f" + pages))
                .build());
    }

    private static String progressBar(double frac) {
        int filled = (int) Math.round(Math.max(0.0, Math.min(1.0, frac)) * 20);
        StringBuilder sb = new StringBuilder("§8[");
        sb.append("§b");
        for (int i = 0; i < filled; i++) sb.append('|');
        sb.append("§8");
        for (int i = filled; i < 20; i++) sb.append('|');
        sb.append("§8]");
        return sb.toString();
    }
}
