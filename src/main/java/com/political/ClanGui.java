package com.political;

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
 * V7 Pillar A — the complete Sorcerer Clans interface. Every action in the system is reachable
 * from here by clicking; {@link ClanCommands} is only a shortcut to this screen.
 */
public final class ClanGui {

    private ClanGui() {}

    private static final int PAGE_SIZE = 36; // slots 9..44

    // ════════════════════════════════════════════════════════════════════
    // MAIN
    // ════════════════════════════════════════════════════════════════════

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Sorcerer Clans").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        String uuid = player.getUUID().toString();
        SorcererClan c = ClanManager.getClanOf(uuid);

        if (c == null) openClanless(gui, player, uuid);
        else openWithClan(gui, player, uuid, c);

        gui.setSlot(49, new GuiElementBuilder(Items.COMPASS)
                .setName(Component.literal("← Civilization Hub").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("§7Return to the master menu."))
                .setCallback((a, b, d, e) -> CivilizationHubGui.open(player))
                .build());
        gui.open();
    }

    private static void openClanless(SimpleGui gui, ServerPlayer player, String uuid) {
        List<SorcererClan> invites = ClanManager.pendingInvitesFor(uuid);

        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal(player.getName().getString())
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7You belong to no clan."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7A clan is an inheritable bloodline with a"))
                .addLoreLine(Component.literal("§7signature technique, treasury and prestige."))
                .glow().build());

        boolean hasSeal = ClanItems.hasSeal(player);
        long coins = CoinManager.getCoinsLong(player);
        boolean canAfford = coins >= ClanManager.FOUND_COST;

        GuiElementBuilder found = new GuiElementBuilder(Items.HEART_OF_THE_SEA)
                .setName(Component.literal("Found a Clan").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Cost: §6" + ClanManager.fmt(ClanManager.FOUND_COST) + " coins"))
                .addLoreLine(Component.literal("§7Plus: §61x Clan Seal §7(craftable)"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Seal: " + (hasSeal ? "§a✓ in inventory" : "§c✗ missing")))
                .addLoreLine(Component.literal("§7Coins: " + (canAfford ? "§a✓ " : "§c✗ ") + ClanManager.fmt(coins)))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(hasSeal && canAfford ? "§eClick to name your clan" : "§8Requirements not met"));
        if (hasSeal && canAfford) found.glow();
        found.setCallback((a, b, d, e) -> {
            if (!ClanItems.hasSeal(player)) {
                player.sendSystemMessage(Component.literal("§cYou need a §6Clan Seal§c. §7See Recipes."));
                return;
            }
            promptFoundName(player);
        });
        gui.setSlot(20, found.build());

        GuiElementBuilder inviteBtn = new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("Pending Invitations").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Clans that have invited you: §f" + invites.size()))
                .addLoreLine(Component.literal(invites.isEmpty() ? "§8Nothing pending" : "§eClick to review"))
                .setCallback((a, b, d, e) -> openInvitations(player));
        if (!invites.isEmpty()) inviteBtn.glow();
        gui.setSlot(22, inviteBtn.build());

        gui.setSlot(24, new GuiElementBuilder(Items.BOOKSHELF)
                .setName(Component.literal("Clan Directory").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Browse every clan, its prestige,"))
                .addLoreLine(Component.literal("§7its Head and its signature technique."))
                .setCallback((a, b, d, e) -> openDirectory(player, 0))
                .build());

        gui.setSlot(31, new GuiElementBuilder(Items.CRAFTING_TABLE)
                .setName(Component.literal("Clan Seal Recipe").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("§7Craft in a 3x3 grid:"))
                .addLoreLine(Component.literal("§8Skull  Gold   Skull"))
                .addLoreLine(Component.literal("§8Gold   Star   Gold"))
                .addLoreLine(Component.literal("§8Skull  GApple Skull"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to open the recipe browser"))
                .setCallback((a, b, d, e) -> RecipesGui.openMainMenu(player))
                .build());
    }

    private static void openWithClan(SimpleGui gui, ServerPlayer player, String uuid, SorcererClan c) {
        SorcererClan.Rank rank = c.rankOf(uuid);
        if (rank == null) rank = SorcererClan.Rank.INITIATE;
        Settlement hall = c.hallSettlementId == null ? null : SettlementManager.get(c.hallSettlementId);
        InheritedTechnique sig = c.signature();

        gui.setSlot(4, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Component.literal(c.displayNameString()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7" + (c.motto == null ? "" : c.motto)))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Prestige level: §b" + c.prestigeLevel + "§8/" + ClanManager.MAX_PRESTIGE_LEVEL))
                .addLoreLine(Component.literal("§7Prestige: §f" + ClanManager.fmt(c.prestige)
                        + (c.prestigeLevel >= ClanManager.MAX_PRESTIGE_LEVEL ? " §8(max)"
                        : " §8(" + ClanManager.fmt(ClanManager.prestigeToNextLevel(c)) + " to next)")))
                .addLoreLine(Component.literal("§7" + progressBar(ClanManager.levelProgress(c))))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Members: §f" + c.memberCount() + "§8/" + SorcererClan.MAX_MEMBERS))
                .addLoreLine(Component.literal("§7Treasury: §6" + ClanManager.fmt(c.treasury) + " coins"))
                .addLoreLine(Component.literal("§7Signature: §d" + sig.display))
                .addLoreLine(Component.literal("§7Hall: §f" + (hall == null ? "§8none" : hall.name)))
                .addLoreLine(Component.literal("§7Your rank: " + rank.colored()))
                .glow().build());

        gui.setSlot(10, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("Roster").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Every member and their rank."))
                .addLoreLine(Component.literal("§eClick to open"))
                .setCallback((a, b, d, e) -> openRoster(player, 0))
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("Treasury").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Balance: §6" + ClanManager.fmt(c.treasury) + " coins"))
                .addLoreLine(Component.literal("§7Deposit open to all, withdraw Elder+."))
                .addLoreLine(Component.literal("§eClick to open"))
                .setCallback((a, b, d, e) -> openTreasury(player))
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Prestige & Perks").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Perks that switch on for every member"))
                .addLoreLine(Component.literal("§7as the clan's prestige rises."))
                .addLoreLine(Component.literal("§eClick to open"))
                .setCallback((a, b, d, e) -> openPerks(player))
                .build());

        gui.setSlot(13, new GuiElementBuilder(sig.guiIcon)
                .setName(Component.literal("Signature Technique").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Current: §d" + sig.display))
                .addLoreLine(Component.literal("§7Channel it, or (Head) choose it."))
                .addLoreLine(Component.literal("§eClick to open"))
                .setCallback((a, b, d, e) -> openSignature(player))
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("Activity Log").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Entries: §f" + c.logList().size()))
                .addLoreLine(Component.literal("§eClick to open"))
                .setCallback((a, b, d, e) -> openLog(player, 0))
                .build());

        gui.setSlot(19, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Component.literal("Feuds").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Rivalries with other clans."))
                .addLoreLine(Component.literal("§7Win a feud for prestige and tribute."))
                .addLoreLine(Component.literal("§eClick to open"))
                .setCallback((a, b, d, e) -> openFeuds(player))
                .build());

        gui.setSlot(20, new GuiElementBuilder(Items.BELL)
                .setName(Component.literal("Clan Hall").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Current: §f" + (hall == null ? "§8none declared" : hall.name)))
                .addLoreLine(Component.literal("§7Declare at a §fTown+ §7settlement for a"))
                .addLoreLine(Component.literal("§7ward on members who stand there."))
                .addLoreLine(Component.literal("§eClick to open"))
                .setCallback((a, b, d, e) -> openHall(player))
                .build());

        boolean canInvite = rank.isAtLeast(SorcererClan.Rank.ELDER);
        gui.setSlot(21, new GuiElementBuilder(canInvite ? Items.WRITABLE_BOOK : Items.BARRIER)
                .setName(Component.literal("Recruit").withStyle(
                        canInvite ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Invite an online, clanless player."))
                .addLoreLine(Component.literal("§7Pending invites: §f" + c.inviteSet().size()))
                .addLoreLine(Component.literal(canInvite ? "§eClick to open" : "§cElder+ only"))
                .setCallback((a, b, d, e) -> {
                    if (!canInvite) {
                        player.sendSystemMessage(Component.literal("§cOnly Elders and the Head may invite."));
                        return;
                    }
                    openRecruit(player, 0);
                })
                .build());

        gui.setSlot(23, new GuiElementBuilder(Items.BOOKSHELF)
                .setName(Component.literal("Clan Directory").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Every clan on the server."))
                .setCallback((a, b, d, e) -> openDirectory(player, 0))
                .build());

        gui.setSlot(24, new GuiElementBuilder(Items.COMPARATOR)
                .setName(Component.literal("Clan Settings").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Motto, crest, disband."))
                .setCallback((a, b, d, e) -> openSettings(player))
                .build());

        // Channel the signature — the marquee action, one click.
        long cd = ClanManager.channelCooldownRemaining(uuid);
        boolean enrolled = SorcererManager.isEnrolled(uuid);
        boolean rankOk = enrolled && SorcererManager.rankOf(uuid).isAtLeast(sig.minRank);
        GuiElementBuilder channel = new GuiElementBuilder(Items.AMETHYST_SHARD)
                .setName(Component.literal("Channel " + sig.display).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7" + String.join(" ", sig.lore)))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Cost: §b" + (long) sig.energyCost + " cursed energy"))
                .addLoreLine(Component.literal("§7Requires: §f" + sig.minRank.display + " sorcerer"))
                .addLoreLine(Component.literal(!enrolled ? "§cAwaken as a sorcerer first"
                        : !rankOk ? "§cYour grade is too low"
                        : cd > 0 ? "§cRecharging: §f" + (cd / 1000L + 1) + "s"
                        : "§eClick to channel"));
        if (rankOk && cd == 0) channel.glow();
        gui.setSlot(31, channel.setCallback((a, b, d, e) -> {
            if (ClanManager.channelSignature(player)) gui.close();
            else open(player);
        }).build());

        gui.setSlot(40, new GuiElementBuilder(Items.OAK_DOOR)
                .setName(Component.literal("Leave Clan").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Give up your rank and perks."))
                .addLoreLine(Component.literal(rank == SorcererClan.Rank.HEAD && c.memberCount() > 1
                        ? "§cPass the headship first."
                        : "§cClick to leave"))
                .setCallback((a, b, d, e) -> {
                    if (ClanManager.leave(player)) open(player);
                })
                .build());

        gui.setSlot(45, new GuiElementBuilder(Items.CLOCK)
                .setName(Component.literal("Your Daily Contribution").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("§7Prestige you have banked today."))
                .addLoreLine(Component.literal("§7Caps stop AFK farms paying out."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Spirits: §f" + ClanManager.dailySpiritPrestige(uuid)
                        + "§8/" + ClanManager.DAILY_SPIRIT_PRESTIGE_CAP))
                .addLoreLine(Component.literal("§7Special Grades: §f" + ClanManager.dailyBossPrestige(uuid)
                        + "§8/" + ClanManager.DAILY_BOSS_PRESTIGE_CAP))
                .build());
    }

    // ════════════════════════════════════════════════════════════════════
    // ROSTER
    // ════════════════════════════════════════════════════════════════════

    public static void openRoster(ServerPlayer player, int page) {
        SorcererClan c = ClanManager.getClanOf(player);
        if (c == null) { open(player); return; }

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Roster — " + c.name).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        List<String> roster = c.rosterSorted();
        int pages = Math.max(1, (roster.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int p = Math.max(0, Math.min(page, pages - 1));

        for (int i = 0; i < PAGE_SIZE; i++) {
            int idx = p * PAGE_SIZE + i;
            if (idx >= roster.size()) break;
            String memberUuid = roster.get(idx);
            SorcererClan.Rank r = c.rankOf(memberUuid);
            String name = DataManager.getPlayerName(memberUuid);
            boolean online = PoliticalServer.server != null
                    && PoliticalServer.server.getPlayerList().getPlayer(java.util.UUID.fromString(memberUuid)) != null;
            gui.setSlot(9 + i, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setName(Component.literal((r == null ? "§7" : r.colorCode) + name).withStyle(ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Rank: " + (r == null ? "§8?" : r.colored())))
                    .addLoreLine(Component.literal("§7Status: " + (online ? "§aonline" : "§8offline")))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to manage"))
                    .setCallback((a, b, d, e) -> openMember(player, memberUuid))
                    .build());
        }

        pager(gui, p, pages, np -> openRoster(player, np));
        gui.setSlot(49, WarGui.back(() -> open(player)));
        gui.open();
    }

    public static void openMember(ServerPlayer player, String targetUuid) {
        SorcererClan c = ClanManager.getClanOf(player);
        if (c == null || targetUuid == null || !c.isMember(targetUuid)) { openRoster(player, 0); return; }

        String actorUuid = player.getUUID().toString();
        SorcererClan.Rank actorRank = c.rankOf(actorUuid);
        SorcererClan.Rank targetRank = c.rankOf(targetUuid);
        if (actorRank == null || targetRank == null) { openRoster(player, 0); return; }
        String name = DataManager.getPlayerName(targetUuid);

        boolean isSelf = actorUuid.equals(targetUuid);
        boolean elder = actorRank.isAtLeast(SorcererClan.Rank.ELDER);
        boolean outranks = actorRank.weight > targetRank.weight;
        boolean canManage = elder && outranks && !isSelf;

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal(name).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        WarGui.fill(gui, 27);

        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal(name).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Rank: " + targetRank.colored()))
                .addLoreLine(Component.literal("§7Clan: §f" + c.name))
                .glow().build());

        gui.setSlot(11, new GuiElementBuilder(canManage ? Items.LIME_DYE : Items.GRAY_DYE)
                .setName(Component.literal("Promote").withStyle(
                        canManage ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7→ " + targetRank.up().colored()))
                .addLoreLine(Component.literal(canManage ? "§eClick to promote" : "§cNot permitted"))
                .setCallback((a, b, d, e) -> {
                    if (ClanManager.promote(player, targetUuid)) openMember(player, targetUuid);
                })
                .build());

        gui.setSlot(13, new GuiElementBuilder(canManage ? Items.ORANGE_DYE : Items.GRAY_DYE)
                .setName(Component.literal("Demote").withStyle(
                        canManage ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7→ " + targetRank.down().colored()))
                .addLoreLine(Component.literal(canManage ? "§eClick to demote" : "§cNot permitted"))
                .setCallback((a, b, d, e) -> {
                    if (ClanManager.demote(player, targetUuid)) openMember(player, targetUuid);
                })
                .build());

        gui.setSlot(15, new GuiElementBuilder(canManage ? Items.BARRIER : Items.GRAY_DYE)
                .setName(Component.literal("Remove from Clan").withStyle(
                        canManage ? ChatFormatting.RED : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(canManage ? "§cClick to remove" : "§cNot permitted"))
                .setCallback((a, b, d, e) -> {
                    if (ClanManager.kick(player, targetUuid)) openRoster(player, 0);
                })
                .build());

        boolean canTransfer = actorRank == SorcererClan.Rank.HEAD && !isSelf;
        gui.setSlot(17, new GuiElementBuilder(canTransfer ? Items.GOLDEN_HELMET : Items.GRAY_DYE)
                .setName(Component.literal("Pass Headship").withStyle(
                        canTransfer ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Make them Clan Head."))
                .addLoreLine(Component.literal("§7You become an Elder."))
                .addLoreLine(Component.literal(canTransfer ? "§cThis cannot be undone" : "§cHead only"))
                .setCallback((a, b, d, e) -> {
                    if (ClanManager.transferHead(player, targetUuid)) openRoster(player, 0);
                })
                .build());

        gui.setSlot(22, WarGui.back(() -> openRoster(player, 0)));
        gui.open();
    }

    // ════════════════════════════════════════════════════════════════════
    // TREASURY
    // ════════════════════════════════════════════════════════════════════

    public static void openTreasury(ServerPlayer player) {
        SorcererClan c = ClanManager.getClanOf(player);
        if (c == null) { open(player); return; }
        String uuid = player.getUUID().toString();
        SorcererClan.Rank rank = c.rankOf(uuid);
        if (rank == null) rank = SorcererClan.Rank.INITIATE;
        boolean elder = rank.isAtLeast(SorcererClan.Rank.ELDER);

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Clan Treasury").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("Treasury").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Clan balance: §6" + ClanManager.fmt(c.treasury)))
                .addLoreLine(Component.literal("§7Your coins: §e" + ClanManager.fmt(CoinManager.getCoinsLong(player))))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Withdrawable today: §f"
                        + ClanManager.fmt(ClanManager.withdrawalHeadroom(c, uuid))))
                .glow().build());

        long[] amounts = {1_000L, 10_000L, 100_000L, 1_000_000L};
        int[] depositSlots = {19, 20, 21, 22};
        for (int i = 0; i < amounts.length; i++) {
            long amt = amounts[i];
            gui.setSlot(depositSlots[i], new GuiElementBuilder(Items.LIME_CONCRETE)
                    .setName(Component.literal("Deposit " + ClanManager.fmt(amt))
                            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Move coins from you to the clan."))
                    .setCallback((a, b, d, e) -> { ClanManager.deposit(player, amt); openTreasury(player); })
                    .build());
        }

        gui.setSlot(23, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("Deposit Custom Amount").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§eClick to type an amount"))
                .setCallback((a, b, d, e) -> promptAmount(player, "Deposit", amt -> {
                    ClanManager.deposit(player, amt);
                    openTreasury(player);
                }))
                .build());

        int[] withdrawSlots = {28, 29, 30, 31};
        for (int i = 0; i < amounts.length; i++) {
            long amt = amounts[i];
            final boolean allowed = elder;
            gui.setSlot(withdrawSlots[i], new GuiElementBuilder(allowed ? Items.RED_CONCRETE : Items.GRAY_CONCRETE)
                    .setName(Component.literal("Withdraw " + ClanManager.fmt(amt))
                            .withStyle(allowed ? ChatFormatting.RED : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(allowed
                            ? "§8Elders capped at " + ClanManager.fmt(ClanManager.ELDER_DAILY_WITHDRAW_CAP) + "/day"
                            : "§cElder+ only"))
                    .setCallback((a, b, d, e) -> {
                        if (!allowed) {
                            player.sendSystemMessage(Component.literal("§cOnly Elders and the Head may withdraw."));
                            return;
                        }
                        ClanManager.withdraw(player, amt);
                        openTreasury(player);
                    })
                    .build());
        }

        final boolean allowedCustom = elder;
        gui.setSlot(32, new GuiElementBuilder(allowedCustom ? Items.PAPER : Items.GRAY_DYE)
                .setName(Component.literal("Withdraw Custom Amount")
                        .withStyle(allowedCustom ? ChatFormatting.RED : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(allowedCustom ? "§eClick to type an amount" : "§cElder+ only"))
                .setCallback((a, b, d, e) -> {
                    if (!allowedCustom) return;
                    promptAmount(player, "Withdraw", amt -> {
                        ClanManager.withdraw(player, amt);
                        openTreasury(player);
                    });
                })
                .build());

        gui.setSlot(49, WarGui.back(() -> open(player)));
        gui.open();
    }

    // ════════════════════════════════════════════════════════════════════
    // PRESTIGE & PERKS
    // ════════════════════════════════════════════════════════════════════

    public static void openPerks(ServerPlayer player) {
        SorcererClan c = ClanManager.getClanOf(player);
        if (c == null) { open(player); return; }

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Prestige & Perks").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal(c.name + " — Prestige Level " + c.prestigeLevel)
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Prestige: §f" + ClanManager.fmt(c.prestige)))
                .addLoreLine(Component.literal("§7" + progressBar(ClanManager.levelProgress(c))))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Prestige is earned by members slaying"))
                .addLoreLine(Component.literal("§7cursed spirits, felling special grades,"))
                .addLoreLine(Component.literal("§7and winning feuds. Perks unlock for all."))
                .glow().build());

        int[] slots = {20, 21, 22, 23, 24};
        ClanPerks.Perk[] perks = ClanPerks.Perk.values();
        for (int i = 0; i < perks.length && i < slots.length; i++) {
            ClanPerks.Perk perk = perks[i];
            boolean active = ClanPerks.isActive(c, perk);
            GuiElementBuilder b = new GuiElementBuilder(active ? perk.icon : Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal((active ? "§a✔ " : "§8") + perk.display).withStyle(ChatFormatting.BOLD));
            for (String line : perk.lore) b.addLoreLine(Component.literal(line));
            b.addLoreLine(Component.literal(""));
            b.addLoreLine(Component.literal("§7Unlocks at prestige level §f" + perk.requiredLevel));
            b.addLoreLine(Component.literal(active ? "§aActive for every member"
                    : "§cLocked — reach level " + perk.requiredLevel));
            if (active) b.glow();
            gui.setSlot(slots[i], b.build());
        }

        gui.setSlot(49, WarGui.back(() -> open(player)));
        gui.open();
    }

    // ════════════════════════════════════════════════════════════════════
    // SIGNATURE TECHNIQUE
    // ════════════════════════════════════════════════════════════════════

    public static void openSignature(ServerPlayer player) {
        SorcererClan c = ClanManager.getClanOf(player);
        if (c == null) { open(player); return; }
        String uuid = player.getUUID().toString();
        boolean isHead = c.rankOf(uuid) == SorcererClan.Rank.HEAD;
        InheritedTechnique sig = c.signature();

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Signature Technique").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        SorcererManager.PlayerSorcerer ps = SorcererManager.getOrNull(uuid);
        double ce = ps == null ? 0 : ps.cursedEnergy;
        double maxCe = ps == null ? 0 : SorcererManager.maxCe(ps);
        long cd = ClanManager.channelCooldownRemaining(uuid);
        boolean enrolled = SorcererManager.isEnrolled(uuid);
        boolean rankOk = enrolled && SorcererManager.rankOf(uuid).isAtLeast(sig.minRank);

        GuiElementBuilder head = new GuiElementBuilder(sig.guiIcon)
                .setName(Component.literal(sig.coloured()).withStyle(ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7" + String.join(" ", sig.lore)))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Cost: §b" + (long) sig.energyCost + " cursed energy"))
                .addLoreLine(Component.literal("§7Cooldown: §f" + (sig.cooldownMs / 1000L) + "s"))
                .addLoreLine(Component.literal("§7Requires: §f" + sig.minRank.display + " sorcerer"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Your cursed energy: §b" + (long) ce + "§8/" + (long) maxCe))
                .addLoreLine(Component.literal(!enrolled ? "§cAwaken as a sorcerer to channel"
                        : !rankOk ? "§cYour grade is too low"
                        : cd > 0 ? "§cRecharging: §f" + (cd / 1000L + 1) + "s"
                        : "§eClick to channel now"));
        if (rankOk && cd == 0) head.glow();
        gui.setSlot(4, head.setCallback((a, b, d, e) -> {
            if (ClanManager.channelSignature(player)) gui.close();
            else openSignature(player);
        }).build());

        // Head can choose the signature from the eight.
        int[] slots = {19, 20, 21, 22, 23, 24, 25, 28};
        InheritedTechnique[] techs = InheritedTechnique.values();
        for (int i = 0; i < techs.length && i < slots.length; i++) {
            InheritedTechnique t = techs[i];
            boolean current = t.id.equals(c.signatureTechniqueId);
            GuiElementBuilder b = new GuiElementBuilder(t.guiIcon)
                    .setName(Component.literal((current ? "§a✔ " : "§d") + t.display).withStyle(ChatFormatting.BOLD));
            for (String line : t.lore) b.addLoreLine(Component.literal(line));
            b.addLoreLine(Component.literal(""));
            b.addLoreLine(Component.literal("§7Cost: §b" + (long) t.energyCost + " CE §8• §7CD §f" + (t.cooldownMs / 1000L) + "s"));
            b.addLoreLine(Component.literal("§7Requires: §f" + t.minRank.display + " sorcerer"));
            b.addLoreLine(Component.literal(""));
            if (current) { b.addLoreLine(Component.literal("§aThis is your clan signature")); b.glow(); }
            else if (isHead) b.addLoreLine(Component.literal("§eClick to make it the signature"));
            else b.addLoreLine(Component.literal("§8Only the Head may change it"));
            b.setCallback((a, x, d, e) -> {
                if (!isHead) return;
                if (ClanManager.setSignature(player, t)) openSignature(player);
            });
            gui.setSlot(slots[i], b.build());
        }

        gui.setSlot(49, WarGui.back(() -> open(player)));
        gui.open();
    }

    // ════════════════════════════════════════════════════════════════════
    // FEUDS
    // ════════════════════════════════════════════════════════════════════

    public static void openFeuds(ServerPlayer player) {
        SorcererClan c = ClanManager.getClanOf(player);
        if (c == null) { open(player); return; }
        boolean isHead = c.rankOf(player.getUUID().toString()) == SorcererClan.Rank.HEAD;

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Feuds — " + c.name).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        gui.setSlot(4, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Component.literal("Clan Feuds").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7While a feud runs, members of the two"))
                .addLoreLine(Component.literal("§7clans deal §c+15% §7damage to each other."))
                .addLoreLine(Component.literal("§7First to §f" + ClanWarBridge.TARGET_SCORE + " §7kills wins prestige"))
                .addLoreLine(Component.literal("§7and a coin tribute from the loser."))
                .addLoreLine(Component.literal(isHead ? "§7Declare feuds from the Directory." : "§8Only the Head declares feuds."))
                .glow().build());

        List<ClanWarBridge.Feud> feuds = ClanWarBridge.feudsOf(c.id);
        int slot = 19;
        for (ClanWarBridge.Feud f : feuds) {
            if (slot > 43) break;
            String otherId = f.clanA.equals(c.id) ? f.clanB : f.clanA;
            SorcererClan other = ClanManager.get(otherId);
            long mine = f.clanA.equals(c.id) ? f.scoreA : f.scoreB;
            long theirs = f.clanA.equals(c.id) ? f.scoreB : f.scoreA;
            gui.setSlot(slot, new GuiElementBuilder(Items.RED_BANNER)
                    .setName(Component.literal("vs " + (other == null ? "?" : other.name))
                            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Your score: §f" + mine + "§8/" + ClanWarBridge.TARGET_SCORE))
                    .addLoreLine(Component.literal("§7Their score: §f" + theirs + "§8/" + ClanWarBridge.TARGET_SCORE))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(isHead ? "§cClick to sue for peace" : "§8Only the Head may end it"))
                    .setCallback((a, b, d, e) -> {
                        if (!isHead || other == null) return;
                        ClanWarBridge.endFeud(c, other);
                        openFeuds(player);
                    })
                    .build());
            slot++;
        }
        if (feuds.isEmpty()) {
            gui.setSlot(31, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("§8No active feuds"))
                    .build());
        }

        gui.setSlot(49, WarGui.back(() -> open(player)));
        gui.open();
    }

    // ════════════════════════════════════════════════════════════════════
    // DIRECTORY / CLAN DETAIL
    // ════════════════════════════════════════════════════════════════════

    public static void openDirectory(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Clan Directory").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        List<SorcererClan> all = ClanManager.leaderboard();
        int pages = Math.max(1, (all.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int p = Math.max(0, Math.min(page, pages - 1));

        for (int i = 0; i < PAGE_SIZE; i++) {
            int idx = p * PAGE_SIZE + i;
            if (idx >= all.size()) break;
            SorcererClan c = all.get(idx);
            gui.setSlot(9 + i, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                    .setName(Component.literal("#" + (idx + 1) + " " + c.displayNameString())
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7" + (c.motto == null ? "" : c.motto)))
                    .addLoreLine(Component.literal("§7Prestige level: §b" + c.prestigeLevel))
                    .addLoreLine(Component.literal("§7Members: §f" + c.memberCount()))
                    .addLoreLine(Component.literal("§7Head: §f" + DataManager.getPlayerName(c.headUuid())))
                    .addLoreLine(Component.literal("§7Signature: §d" + c.signature().display))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to view"))
                    .setCallback((a, b, d, e) -> openClanDetail(player, c.id))
                    .build());
        }
        if (all.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("§8No clans have been founded yet"))
                    .build());
        }

        pager(gui, p, pages, np -> openDirectory(player, np));
        gui.setSlot(49, WarGui.back(() -> open(player)));
        gui.open();
    }

    public static void openClanDetail(ServerPlayer player, String clanId) {
        SorcererClan c = ClanManager.get(clanId);
        if (c == null) { openDirectory(player, 0); return; }
        SorcererClan mine = ClanManager.getClanOf(player);
        boolean isHeadOfMine = mine != null && mine.rankOf(player.getUUID().toString()) == SorcererClan.Rank.HEAD;
        boolean isOther = mine == null || !mine.id.equals(c.id);
        boolean feuding = mine != null && ClanWarBridge.areFeuding(mine.id, c.id);

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal(c.name).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        WarGui.fill(gui, 27);

        gui.setSlot(4, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Component.literal(c.displayNameString()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7" + (c.motto == null ? "" : c.motto)))
                .addLoreLine(Component.literal("§7Prestige level: §b" + c.prestigeLevel))
                .addLoreLine(Component.literal("§7Members: §f" + c.memberCount()))
                .addLoreLine(Component.literal("§7Head: §f" + DataManager.getPlayerName(c.headUuid())))
                .addLoreLine(Component.literal("§7Signature: §d" + c.signature().display))
                .glow().build());

        boolean canFeud = isHeadOfMine && isOther && !feuding;
        gui.setSlot(12, new GuiElementBuilder(canFeud ? Items.NETHERITE_SWORD : Items.GRAY_DYE)
                .setName(Component.literal("Declare Feud").withStyle(
                        canFeud ? ChatFormatting.DARK_RED : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Open a scored rivalry with this clan."))
                .addLoreLine(Component.literal(!isHeadOfMine ? "§cYou must be a Clan Head"
                        : !isOther ? "§cThat is your own clan"
                        : feuding ? "§cAlready feuding" : "§eClick to declare"))
                .setCallback((a, b, d, e) -> {
                    if (!canFeud) return;
                    ClanWarBridge.declareFeud(mine, c);
                    openClanDetail(player, clanId);
                })
                .build());

        boolean canPeace = isHeadOfMine && feuding;
        gui.setSlot(14, new GuiElementBuilder(canPeace ? Items.WHITE_BANNER : Items.GRAY_DYE)
                .setName(Component.literal("Sue for Peace").withStyle(
                        canPeace ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7End the feud with no winner."))
                .addLoreLine(Component.literal(canPeace ? "§eClick to end the feud" : "§cNo feud to end"))
                .setCallback((a, b, d, e) -> {
                    if (!canPeace) return;
                    ClanWarBridge.endFeud(mine, c);
                    openClanDetail(player, clanId);
                })
                .build());

        gui.setSlot(22, WarGui.back(() -> openDirectory(player, 0)));
        gui.open();
    }

    // ════════════════════════════════════════════════════════════════════
    // HALL
    // ════════════════════════════════════════════════════════════════════

    public static void openHall(ServerPlayer player) {
        SorcererClan c = ClanManager.getClanOf(player);
        if (c == null) { open(player); return; }
        boolean isHead = c.rankOf(player.getUUID().toString()) == SorcererClan.Rank.HEAD;

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Clan Hall").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        Settlement hall = c.hallSettlementId == null ? null : SettlementManager.get(c.hallSettlementId);
        Settlement here = CivilizationHubGui.currentOrHome(player);
        int residents = 0;
        if (hall != null) {
            for (String u : c.memberMap().keySet()) {
                if (hall.registered().contains(u)) residents++;
            }
        }

        gui.setSlot(4, new GuiElementBuilder(Items.BELL)
                .setName(Component.literal("Current Hall").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Settlement: §f" + (hall == null ? "§8none declared" : hall.name)))
                .addLoreLine(Component.literal(hall == null ? "" : "§7Tier: " + hall.tierEnum().colored()))
                .addLoreLine(Component.literal(hall == null ? "" : "§7Resident members: §f" + residents))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7With the Ancestral Hall perk, members"))
                .addLoreLine(Component.literal("§7standing here gain Resistance and Regen."))
                .glow().build());

        boolean canDeclare = isHead && here != null
                && here.tierEnum().isAtLeast(SettlementTier.TOWN)
                && (hall == null || !here.id.equals(hall.id));

        gui.setSlot(20, new GuiElementBuilder(canDeclare ? Items.LIME_CONCRETE : Items.GRAY_CONCRETE)
                .setName(Component.literal("Declare Hall Here").withStyle(
                        canDeclare ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Settlement: §f" + (here == null ? "§8none nearby" : here.name)))
                .addLoreLine(Component.literal("§7Requires: §fTown+§7, §f"
                        + ClanManager.HALL_MIN_RESIDENT_MEMBERS + " §7resident members"))
                .addLoreLine(Component.literal("§7Cost: §6" + ClanManager.fmt(ClanManager.HALL_DECLARE_COST)
                        + " coins §7from the treasury"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(isHead
                        ? (canDeclare ? "§eClick to declare" : "§cRequirements not met here")
                        : "§cClan Head only"))
                .setCallback((a, b, d, e) -> {
                    Settlement s = CivilizationHubGui.currentOrHome(player);
                    if (s == null) {
                        player.sendSystemMessage(Component.literal("§cStand in a settlement first."));
                        return;
                    }
                    if (ClanManager.declareHall(player, s)) openHall(player);
                })
                .build());

        boolean canAbandon = isHead && hall != null;
        gui.setSlot(24, new GuiElementBuilder(canAbandon ? Items.BARRIER : Items.GRAY_DYE)
                .setName(Component.literal("Abandon Hall").withStyle(
                        canAbandon ? ChatFormatting.RED : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7The declaration fee is not refunded."))
                .addLoreLine(Component.literal(canAbandon ? "§cClick to abandon" : "§cNothing to abandon"))
                .setCallback((a, b, d, e) -> {
                    if (ClanManager.abandonHall(player)) openHall(player);
                })
                .build());

        gui.setSlot(49, WarGui.back(() -> open(player)));
        gui.open();
    }

    // ════════════════════════════════════════════════════════════════════
    // RECRUIT / INVITATIONS
    // ════════════════════════════════════════════════════════════════════

    public static void openRecruit(ServerPlayer player, int page) {
        SorcererClan c = ClanManager.getClanOf(player);
        if (c == null) { open(player); return; }

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Recruit").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        List<ServerPlayer> candidates = new ArrayList<>();
        if (PoliticalServer.server != null) {
            for (ServerPlayer pl : PoliticalServer.server.getPlayerList().getPlayers()) {
                if (ClanManager.getClanOf(pl) == null) candidates.add(pl);
            }
        }

        int pages = Math.max(1, (candidates.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int pg = Math.max(0, Math.min(page, pages - 1));
        for (int i = 0; i < PAGE_SIZE; i++) {
            int idx = pg * PAGE_SIZE + i;
            if (idx >= candidates.size()) break;
            ServerPlayer target = candidates.get(idx);
            boolean invited = c.inviteSet().contains(target.getUUID().toString());
            gui.setSlot(9 + i, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setName(Component.literal(target.getName().getString())
                            .withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Clanless."))
                    .addLoreLine(Component.literal(invited ? "§aInvite already sent" : "§eClick to invite"))
                    .setCallback((a, b, d, e) -> { ClanManager.invite(player, target); openRecruit(player, pg); })
                    .build());
        }
        if (candidates.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("§8No clanless players are online"))
                    .build());
        }

        pager(gui, pg, pages, np -> openRecruit(player, np));
        gui.setSlot(49, WarGui.back(() -> open(player)));
        gui.open();
    }

    public static void openInvitations(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        List<SorcererClan> invites = ClanManager.pendingInvitesFor(uuid);

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Invitations").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        int slot = 10;
        for (SorcererClan c : invites) {
            if (slot > 43) break;
            gui.setSlot(slot, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                    .setName(Component.literal(c.displayNameString()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7" + (c.motto == null ? "" : c.motto)))
                    .addLoreLine(Component.literal("§7Prestige level §b" + c.prestigeLevel + " §7• §f" + c.memberCount() + " members"))
                    .addLoreLine(Component.literal("§7Head: §f" + DataManager.getPlayerName(c.headUuid())))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§aLeft-click to accept"))
                    .addLoreLine(Component.literal("§cRight-click to decline"))
                    .setCallback((index, clickType, action, slotGui) -> {
                        if (clickType.isRight) ClanManager.declineInvite(player, c.id);
                        else ClanManager.acceptInvite(player, c.id);
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
    // LOG / SETTINGS
    // ════════════════════════════════════════════════════════════════════

    public static void openLog(ServerPlayer player, int page) {
        SorcererClan c = ClanManager.getClanOf(player);
        if (c == null) { open(player); return; }

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Clan Log").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        List<String> entries = new ArrayList<>(c.logList());
        java.util.Collections.reverse(entries);
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

    public static void openSettings(ServerPlayer player) {
        SorcererClan c = ClanManager.getClanOf(player);
        if (c == null) { open(player); return; }
        String uuid = player.getUUID().toString();
        SorcererClan.Rank rank = c.rankOf(uuid);
        if (rank == null) rank = SorcererClan.Rank.INITIATE;
        boolean isHead = rank == SorcererClan.Rank.HEAD;
        boolean elder = rank.isAtLeast(SorcererClan.Rank.ELDER);

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Clan Settings").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        gui.setSlot(4, new GuiElementBuilder(Items.NAME_TAG)
                .setName(Component.literal(c.displayNameString()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7" + (c.motto == null || c.motto.isEmpty() ? "§8no motto" : c.motto)))
                .glow().build());

        final boolean canMotto = elder;
        gui.setSlot(20, new GuiElementBuilder(canMotto ? Items.WRITABLE_BOOK : Items.GRAY_DYE)
                .setName(Component.literal("Set Motto").withStyle(
                        canMotto ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(canMotto ? "§eClick to set" : "§cElder+ only"))
                .setCallback((a, b, d, e) -> {
                    if (!canMotto) return;
                    SignGui sign = new SignGui(player) {
                        @Override
                        public void onManualClose() {
                            ClanManager.setMotto(player, this.getLine(0).getString().trim());
                            openSettings(player);
                        }
                    };
                    sign.setLine(1, Component.literal("^ Clan motto"));
                    sign.setLine(2, Component.literal("Max 48 chars"));
                    sign.open();
                })
                .build());

        gui.setSlot(22, new GuiElementBuilder(isHead ? Items.NETHER_STAR : Items.GRAY_DYE)
                .setName(Component.literal("Set Crest").withStyle(
                        isHead ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7A single symbol shown before the name."))
                .addLoreLine(Component.literal(isHead ? "§eClick to set" : "§cClan Head only"))
                .setCallback((a, b, d, e) -> {
                    if (!isHead) return;
                    SignGui sign = new SignGui(player) {
                        @Override
                        public void onManualClose() {
                            ClanManager.setCrest(player, this.getLine(0).getString().trim());
                            openSettings(player);
                        }
                    };
                    sign.setLine(1, Component.literal("^ One symbol"));
                    sign.setLine(2, Component.literal("e.g. an emblem"));
                    sign.open();
                })
                .build());

        gui.setSlot(24, new GuiElementBuilder(isHead ? Items.TNT : Items.GRAY_DYE)
                .setName(Component.literal("Disband Clan").withStyle(
                        isHead ? ChatFormatting.DARK_RED : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§cPermanently destroys the clan."))
                .addLoreLine(Component.literal("§cThe treasury is forfeit."))
                .addLoreLine(Component.literal(isHead ? "§4Confirm on the next screen" : "§cClan Head only"))
                .setCallback((a, b, d, e) -> {
                    if (!isHead) return;
                    confirmDisband(player);
                })
                .build());

        gui.setSlot(49, WarGui.back(() -> open(player)));
        gui.open();
    }

    private static void confirmDisband(ServerPlayer player) {
        SorcererClan c = ClanManager.getClanOf(player);
        if (c == null) { open(player); return; }

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("Disband " + c.name + "?").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        WarGui.fill(gui, 27);

        gui.setSlot(11, new GuiElementBuilder(Items.RED_CONCRETE)
                .setName(Component.literal("YES — disband").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§cThis cannot be undone."))
                .addLoreLine(Component.literal("§c" + ClanManager.fmt(c.treasury) + " coins will be lost."))
                .setCallback((a, b, d, e) -> { ClanManager.disband(player, c); open(player); })
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.LIME_CONCRETE)
                .setName(Component.literal("NO — keep the clan").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .setCallback((a, b, d, e) -> openSettings(player))
                .build());
        gui.open();
    }

    // ════════════════════════════════════════════════════════════════════
    // TEXT PROMPTS
    // ════════════════════════════════════════════════════════════════════

    private static void promptFoundName(ServerPlayer player) {
        SignGui sign = new SignGui(player) {
            @Override
            public void onManualClose() {
                String name = ClanManager.sanitizeName(this.getLine(0).getString());
                if (name.length() < 3) {
                    player.sendSystemMessage(Component.literal("§cClan names must be at least 3 characters."));
                    ClanGui.open(player);
                    return;
                }
                ClanManager.foundClan(player, name);
                ClanGui.open(player);
            }
        };
        sign.setLine(1, Component.literal("^ Clan name"));
        sign.setLine(2, Component.literal("3-24 characters"));
        sign.open();
    }

    private interface AmountConsumer {
        void accept(long amount);
    }

    private static void promptAmount(ServerPlayer player, String label, AmountConsumer consumer) {
        SignGui sign = new SignGui(player) {
            @Override
            public void onManualClose() {
                String raw = this.getLine(0).getString().trim().replaceAll("[^0-9]", "");
                if (raw.isEmpty()) { openTreasury(player); return; }
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
        StringBuilder sb = new StringBuilder("§8[§b");
        for (int i = 0; i < filled; i++) sb.append('|');
        sb.append("§8");
        for (int i = filled; i < 20; i++) sb.append('|');
        sb.append("§8]");
        return sb.toString();
    }
}
