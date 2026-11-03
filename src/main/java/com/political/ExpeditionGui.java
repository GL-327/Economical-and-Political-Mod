package com.political;


import com.political.economy.CoinManager;
import com.political.settlement.Settlement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/**
 * V5 Pillar 1 — the whole Expedition system, playable entirely by clicking.
 *
 * <p>Screens:
 * <ul>
 *   <li>{@link #open} — tier picker with live requirements, modifiers, rewards and cooldown,
 *       plus a live progress panel and an Abandon button while a run is in flight.</li>
 *   <li>{@link #openTier} — a per-tier detail/confirm screen with the Sigil toggle.</li>
 * </ul>
 * Every screen refreshes by re-calling its opener; no command is ever required.
 */
public final class ExpeditionGui {

    private ExpeditionGui() {}

    private static final int[] TIER_SLOTS = {19, 20, 21, 22, 23};

    // ════════════════════════════════════════════════════════════
    // MAIN SCREEN
    // ════════════════════════════════════════════════════════════

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Expeditions").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        fill(gui, 54);

        String uuid = player.getUUID().toString();
        ExpeditionDungeon run = ExpeditionRunManager.runOf(uuid);
        long cd = ExpeditionRunManager.cooldownRemaining(uuid);
        int keys = ExpeditionRewards.countKeys(player);
        int sigils = ExpeditionRewards.countSigils(player);
        Settlement home = ExpeditionRunManager.launchSettlement(player);

        // ── header ──
        GuiElementBuilder header = new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal(player.getName().getString() + " — Delver")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Coins: §6" + String.format("%,d",
                        CoinManager.getCoinsLong(player))))
                .addLoreLine(Component.literal("§7Launch settlement: §f"
                        + (home == null ? "§8none" : home.name + " §7(" + home.tierEnum().colored() + "§7)")))
                .addLoreLine(Component.literal("§7Expedition Keys: §e" + keys))
                .addLoreLine(Component.literal("§7Expedition Sigils: §b" + sigils))
                .addLoreLine(Component.literal("§7Cooldown: "
                        + (cd <= 0 ? "§aready" : "§c" + ExpeditionRunManager.formatDuration(cd))))
                .glow();
        gui.setSlot(4, header.build());

        // ── live run panel ──
        if (run != null) {
            ExpeditionRunManager.Tier t = ExpeditionRunManager.Tier.byName(run.tier);
            long left = Math.max(0L, run.deadlineAt - System.currentTimeMillis());
            gui.setSlot(10, new GuiElementBuilder(Items.COMPASS)
                    .setName(Component.literal("Run in progress")
                            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Tier: " + colorOf(t) + t.displayName))
                    .addLoreLine(Component.literal("§7Floor: §f" + run.floor + "§7/§f" + run.totalFloors))
                    .addLoreLine(Component.literal("§7Rooms: §f" + run.roomsCleared
                            + "§7/§f" + run.roomsTotalThisFloor))
                    .addLoreLine(Component.literal("§7Party: §f" + run.party().size()))
                    .addLoreLine(Component.literal("§7Time left: §c"
                            + ExpeditionRunManager.formatDuration(left)))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to refresh"))
                    .glow()
                    .setCallback((a, b, c, d) -> open(player))
                    .build());

            gui.setSlot(16, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("Abandon Run")
                            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Leave immediately with §cno rewards§7."))
                    .addLoreLine(Component.literal("§7You still take §chalf the cooldown§7."))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§cClick to abandon"))
                    .setCallback((a, b, c, d) -> {
                        ExpeditionRunManager.abandon(player);
                        open(player);
                    })
                    .build());
        } else {
            gui.setSlot(10, new GuiElementBuilder(Items.MAP)
                    .setName(Component.literal("No active run")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Pick a tier below to descend."))
                    .addLoreLine(Component.literal("§8If you lead a party, every online"))
                    .addLoreLine(Component.literal("§8member is pulled in with you."))
                    .build());
        }

        // ── tier picker ──
        int i = 0;
        for (ExpeditionRunManager.Tier tier : ExpeditionRunManager.Tier.values()) {
            gui.setSlot(TIER_SLOTS[i++], tierButton(player, tier, run != null));
        }

        // ── footer ──
        gui.setSlot(48, new GuiElementBuilder(Items.TRIPWIRE_HOOK)
                .setName(Component.literal("Expedition Keys").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7You hold: §e" + keys))
                .addLoreLine(Component.literal("§7Spent automatically to bypass a cooldown."))
                .addLoreLine(Component.literal("§8Not craftable — dropped on completion."))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("How Expeditions work").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Each floor is a 3x3 grid of rooms."))
                .addLoreLine(Component.literal("§7Rooms only arm when you walk in."))
                .addLoreLine(Component.literal("§7Combat rooms: kill everything."))
                .addLoreLine(Component.literal("§7Puzzle room: throw all three levers."))
                .addLoreLine(Component.literal("§7Vault: kill the guards, loot the chests."))
                .addLoreLine(Component.literal("§7Final floor ends in a boss arena."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§cMiss the clock and you get nothing."))
                .build());

        gui.setSlot(50, new GuiElementBuilder(Items.HEART_OF_THE_SEA)
                .setName(Component.literal("Expedition Sigils").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7You hold: §b" + sigils))
                .addLoreLine(Component.literal("§7Optional: §b+1 loot roll §7on completion."))
                .addLoreLine(Component.literal("§8Craftable — see the Recipes menu."))
                .build());

        gui.setSlot(53, new GuiElementBuilder(Items.OAK_DOOR)
                .setName(Component.literal("Back to Civilization").withStyle(ChatFormatting.GRAY))
                .setCallback((a, b, c, d) -> CivilizationHubGui.open(player))
                .build());

        gui.open();
    }

    private static eu.pb4.sgui.api.elements.GuiElement tierButton(ServerPlayer player,
                                                                  ExpeditionRunManager.Tier tier,
                                                                  boolean busy) {
        String block = busy ? "You are already inside an expedition."
                : ExpeditionRunManager.blockReason(player, tier);
        boolean available = block == null;

        GuiElementBuilder b = new GuiElementBuilder(iconFor(tier))
                .setName(Component.literal(tier.displayName + "  §8[T" + tier.level + "]")
                        .withStyle(tier.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(tier.modifierLine()))
                .addLoreLine(Component.literal("§7Floors: §f" + tier.floors
                        + "   §7Time limit: §c" + tier.timeLimitMinutes + "m"))
                .addLoreLine(Component.literal("§7Pack size: §f" + tier.mobsPerRoom + " per room"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§8── Rewards ──"))
                .addLoreLine(Component.literal("§7Coins: §6" + String.format("%,d",
                        ExpeditionRewards.coinReward(tier.level))))
                .addLoreLine(Component.literal("§7Arcane Shards: §d+"
                        + ExpeditionRewards.shardReward(tier.level)))
                .addLoreLine(Component.literal("§7Gear roll: §f"
                        + Math.round(ExpeditionRewards.gearChance(tier.level) * 100) + "%"))
                .addLoreLine(Component.literal("§7Key drop: §f"
                        + Math.round(ExpeditionRewards.keyChance(tier.level) * 100) + "%"))
                .addLoreLine(Component.literal("§7Settlement treasury: §6+"
                        + Math.round(ExpeditionRewards.SETTLEMENT_TREASURY_SHARE * 100) + "%"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§8── Cost ──"))
                .addLoreLine(Component.literal("§7Entry: §6" + String.format("%,d",
                        ExpeditionRewards.entryCost(tier.level)) + " coins"))
                .addLoreLine(Component.literal("§7Cooldown after: §c" + tier.cooldownMinutes() + "m"))
                .addLoreLine(Component.literal("§7Settlement gate: §f"
                        + (tier.requiredSettlement == null ? "§anone"
                        : tier.requiredSettlement.colored() + "§7+")))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Your clears: §f"
                        + ExpeditionRunManager.clearsOf(player.getUUID().toString(), tier)));

        if (available) {
            b.glow();
            b.addLoreLine(Component.literal("§aClick to inspect and launch"));
            b.setCallback((a, c, d, e) -> openTier(player, tier, false));
        } else {
            b.addLoreLine(Component.literal("§c✖ " + block));
        }
        return b.build();
    }

    // ════════════════════════════════════════════════════════════
    // TIER DETAIL / CONFIRM
    // ════════════════════════════════════════════════════════════

    public static void openTier(ServerPlayer player, ExpeditionRunManager.Tier tier, boolean useSigil) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("Expedition — " + tier.displayName)
                .withStyle(tier.color, ChatFormatting.BOLD));
        fill(gui, 27);

        int sigils = ExpeditionRewards.countSigils(player);
        boolean sigilOn = useSigil && sigils > 0;
        String block = ExpeditionRunManager.blockReason(player, tier);

        gui.setSlot(4, new GuiElementBuilder(iconFor(tier))
                .setName(Component.literal(tier.displayName).withStyle(tier.color, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(tier.modifierLine()))
                .addLoreLine(Component.literal("§7" + tier.floors + " floor(s), "
                        + tier.timeLimitMinutes + " minute limit."))
                .addLoreLine(Component.literal("§7Boss waits on the final floor."))
                .glow().build());

        // Party preview
        String uuid = player.getUUID().toString();
        String partyLeader = PartyManager.getPartyLeader(uuid);
        boolean leadsParty = partyLeader != null && partyLeader.equals(uuid);
        int partySize = leadsParty ? PartyManager.getPartyMembers(uuid).size() : 1;
        gui.setSlot(10, new GuiElementBuilder(Items.BANNER.white())
                .setName(Component.literal("Party").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Entering with: §f" + partySize + " player(s)"))
                .addLoreLine(Component.literal(leadsParty
                        ? "§8Every online party member is pulled in."
                        : "§8You are entering solo. Lead a party to bring others."))
                .build());

        // Sigil toggle
        gui.setSlot(12, new GuiElementBuilder(sigilOn ? Items.HEART_OF_THE_SEA : Items.NAUTILUS_SHELL)
                .setName(Component.literal("Use Expedition Sigil: "
                        + (sigilOn ? "§aON" : "§cOFF"))
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Held: §b" + sigils))
                .addLoreLine(Component.literal("§7Consumes one sigil for §b+1 loot roll§7."))
                .addLoreLine(Component.literal("§8Lost if the run fails or is abandoned."))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(sigils > 0 ? "§eClick to toggle" : "§8You have no sigils."))
                .setCallback((a, b, c, d) -> {
                    if (sigils > 0) openTier(player, tier, !sigilOn);
                })
                .build());

        // Launch
        if (block == null) {
            gui.setSlot(14, new GuiElementBuilder(Items.ENDER_EYE)
                    .setName(Component.literal("LAUNCH EXPEDITION")
                            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Entry: §6" + String.format("%,d",
                            ExpeditionRewards.entryCost(tier.level)) + " coins"))
                    .addLoreLine(Component.literal(sigilOn ? "§bSigil will be consumed." : "§8No sigil."))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§aClick to descend"))
                    .glow()
                    .setCallback((a, b, c, d) -> {
                        gui.close();
                        ExpeditionRunManager.startRun(player, tier, sigilOn);
                    })
                    .build());
        } else {
            gui.setSlot(14, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("Cannot launch").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§c" + block))
                    .build());
        }

        gui.setSlot(16, new GuiElementBuilder(Items.CHEST)
                .setName(Component.literal("Reward Preview").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§6" + String.format("%,d",
                        ExpeditionRewards.coinReward(tier.level)) + " §7coins"))
                .addLoreLine(Component.literal("§d+" + ExpeditionRewards.shardReward(tier.level)
                        + " §7Arcane Shards"))
                .addLoreLine(Component.literal("§7Delver's Blade / Vaultwarden Plate /"))
                .addLoreLine(Component.literal("§7Abyssal Circlet at tier §f" + tier.level))
                .addLoreLine(Component.literal("§7Vault chests along the way."))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.OAK_DOOR)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((a, b, c, d) -> open(player))
                .build());

        gui.open();
    }

    // ════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════

    private static net.minecraft.world.item.Item iconFor(ExpeditionRunManager.Tier tier) {
        return switch (tier) {
            case DELVE -> Items.STONE_SWORD;
            case DESCENT -> Items.IRON_SWORD;
            case ABYSS -> Items.DIAMOND_SWORD;
            case CATACLYSM -> Items.NETHERITE_SWORD;
            case APOTHEOSIS -> Items.NETHER_STAR;
        };
    }

    private static String colorOf(ExpeditionRunManager.Tier tier) {
        return switch (tier) {
            case DELVE -> "§a";
            case DESCENT -> "§e";
            case ABYSS -> "§6";
            case CATACLYSM -> "§c";
            case APOTHEOSIS -> "§d";
        };
    }

    /** Neutral filler so empty slots are not clickable voids. */
    /** A flat grey wall became a framed screen, matching everything built since. */
    private static void fill(SimpleGui gui, int size) {
        GuiKit.frame(gui, size);
    }
}
