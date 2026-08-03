package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** The War Room — declare, debate and end wars entirely through the GUI. */
public final class WarGui {

    private WarGui() {}

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("War Room").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        fill(gui, 54);

        Settlement polity = WarManager.polityOf(player);
        String uuid = player.getUUID().toString();

        if (polity == null) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("No Polity").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Register at a settlement to gain a voice."))
                    .addLoreLine(Component.literal("§8Only METROPOLIS+ realms are sovereign."))
                    .build());
            gui.open();
            return;
        }

        WarManager.WarRole role = WarManager.roleOf(polity.id, uuid);

        // Header — your polity and seat
        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal(polity.name).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Your seat: " + (role == null ? "§8none"
                        : "§f" + role.displayName + " §7(weight §f" + role.weight + "§7)")))
                .addLoreLine(Component.literal(role == null ? "§8You may observe, not vote."
                        : role.official ? "§bElected / appointed office" : "§8Unelected influence"))
                .glow().build());

        // ── Active motion ──
        WarManager.WarMotion motion = WarManager.openMotionFor(polity.id);
        if (motion != null) {
            Settlement target = SettlementManager.get(motion.targetPolityId);
            int[] t = WarManager.tally(motion);
            long secs = Math.max(0, motion.closesAtMs - System.currentTimeMillis()) / 1000;

            GuiElementBuilder m = new GuiElementBuilder(Items.NETHERITE_SWORD)
                    .setName(Component.literal("Motion: war on " + (target != null ? target.name : "?"))
                            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Tabled by §f"
                            + DataManager.getPlayerName(motion.proposerUuid)))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7For: §a" + t[0] + "   §7Against: §c" + t[1]))
                    .addLoreLine(Component.literal("§7Closes in §f" + secs + "s"));
            if (t[2] == 1) m.addLoreLine(Component.literal("§4§lCHAIR VETO — this motion will fail"));
            gui.setSlot(13, m.glow().build());

            boolean canVote = role != null;
            Boolean myVote = motion.votes.get(uuid);

            gui.setSlot(29, new GuiElementBuilder(canVote ? Items.LIME_CONCRETE : Items.GRAY_CONCRETE)
                    .setName(Component.literal("Vote FOR war")
                            .withStyle(canVote ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(canVote
                            ? (Boolean.TRUE.equals(myVote) ? "§a§lYour current vote" : "§eClick to vote for")
                            : "§cYou hold no council seat"))
                    .setCallback((a, b, c, d) -> {
                        if (canVote && WarManager.castVote(player, true)) open(player);
                    })
                    .build());

            gui.setSlot(33, new GuiElementBuilder(canVote ? Items.RED_CONCRETE : Items.GRAY_CONCRETE)
                    .setName(Component.literal("Vote AGAINST war")
                            .withStyle(canVote ? ChatFormatting.RED : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(canVote
                            ? (Boolean.FALSE.equals(myVote) ? "§c§lYour current vote" : "§eClick to vote against")
                            : "§cYou hold no council seat"))
                    .addLoreLine(Component.literal(role == WarManager.WarRole.CHAIR
                            ? "§4As Chair this is an absolute veto." : ""))
                    .setCallback((a, b, c, d) -> {
                        if (canVote && WarManager.castVote(player, false)) open(player);
                    })
                    .build());
        } else {
            gui.setSlot(13, new GuiElementBuilder(Items.WHITE_BANNER)
                    .setName(Component.literal("No motion before the council")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Peace, for now."))
                    .build());
        }

        // ── Declare war (target picker) ──
        boolean canPropose = role != null;
        gui.setSlot(20, new GuiElementBuilder(canPropose ? Items.IRON_SWORD : Items.GRAY_CONCRETE)
                .setName(Component.literal("Declare War")
                        .withStyle(canPropose ? ChatFormatting.RED : ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(canPropose
                        ? "§7Table a motion against another realm."
                        : "§cOnly the War Council may table a motion."))
                .setCallback((a, b, c, d) -> {
                    if (canPropose) openTargetPicker(player, polity);
                })
                .build());

        // ── Council breakdown ──
        gui.setSlot(22, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("War Council").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7See every seat and its weight."))
                .setCallback((a, b, c, d) -> openCouncil(player, polity))
                .build());

        // ── Shadow government ──
        gui.setSlot(24, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Component.literal("Shadow Government").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Unelected power — real, but subordinate."))
                .setCallback((a, b, c, d) -> openShadow(player, polity))
                .build());

        // ── Peace ──
        boolean atWar = WarManager.isAtWar(polity.id);
        boolean canSue = role == WarManager.WarRole.CHAIR || role == WarManager.WarRole.VICE_CHAIR
                || role == WarManager.WarRole.PRIME_SEAT;
        gui.setSlot(40, new GuiElementBuilder(atWar && canSue ? Items.WHITE_BANNER : Items.GRAY_CONCRETE)
                .setName(Component.literal("Sue for Peace")
                        .withStyle(atWar && canSue ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY,
                                ChatFormatting.BOLD))
                .addLoreLine(Component.literal(!atWar ? "§8Your realm is not at war."
                        : canSue ? "§7Offer terms. The enemy must reciprocate."
                        : "§cOnly Chair, Vice Chair or Prime Seat may sue."))
                .setCallback((a, b, c, d) -> {
                    if (atWar && canSue) {
                        WarManager.proposePeace(player);
                        open(player);
                    }
                })
                .build());

        // ── Active wars list ──
        List<WarManager.War> active = WarManager.activeWars();
        GuiElementBuilder warsBtn = new GuiElementBuilder(active.isEmpty() ? Items.MAP : Items.FILLED_MAP)
                .setName(Component.literal("Active Wars (" + active.size() + ")")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        if (active.isEmpty()) {
            warsBtn.addLoreLine(Component.literal("§7The world is at peace."));
        } else {
            for (WarManager.War w : active) {
                Settlement a2 = SettlementManager.get(w.aId);
                Settlement b2 = SettlementManager.get(w.bId);
                warsBtn.addLoreLine(Component.literal("§c" + (a2 != null ? a2.name : "?")
                        + " §7vs §c" + (b2 != null ? b2.name : "?")
                        + " §8(" + w.scoreA + "–" + w.scoreB + ")"));
            }
        }
        gui.setSlot(49, warsBtn.build());

        gui.open();
    }

    // ── Target picker ──

    private static void openTargetPicker(ServerPlayer player, Settlement polity) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x5, player, false);
        gui.setTitle(Component.literal("Declare War On...").withStyle(ChatFormatting.DARK_RED));
        fill(gui, 45);

        List<Settlement> targets = new ArrayList<>();
        for (Settlement s : SettlementManager.getAllSettlements()) {
            if (s.id.equals(polity.id)) continue;
            if (!s.tierEnum().isAtLeast(SettlementTier.METROPOLIS)) continue;
            targets.add(s);
        }

        if (targets.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("No rival realms").withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal("§7Only METROPOLIS+ governments can be warred upon."))
                    .build());
        }

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        for (int i = 0; i < targets.size() && i < slots.length; i++) {
            Settlement t = targets.get(i);
            boolean already = WarManager.warBetween(polity.id, t.id) != null;
            SettlementTier tier = t.tierEnum();
            gui.setSlot(slots[i], new GuiElementBuilder(already ? Items.REDSTONE_BLOCK : Items.IRON_SWORD)
                    .setName(Component.literal(t.name).withStyle(tier.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Tier: " + tier.colorCode + tier.displayName))
                    .addLoreLine(Component.literal("§7Population: §f" + t.population
                            + "   §7Defense: §c" + t.defense))
                    .addLoreLine(Component.literal("§7Treasury: §6" + t.treasury))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(already ? "§cAlready at war" : "§eClick to table a war motion"))
                    .setCallback((a, b, c, d) -> {
                        if (!already && WarManager.proposeWar(player, t)) {
                            open(player);
                        }
                    })
                    .build());
        }

        gui.setSlot(36, back(() -> open(player)));
        gui.open();
    }

    // ── Council view ──

    private static void openCouncil(ServerPlayer player, Settlement polity) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("War Council — " + polity.name));
        fill(gui, 54);

        Map<String, WarManager.WarRole> council = WarManager.councilOf(polity.id);
        List<Map.Entry<String, WarManager.WarRole>> entries = new ArrayList<>(council.entrySet());
        entries.sort((x, y) -> Integer.compare(y.getValue().weight, x.getValue().weight));

        int officialWeight = 0, shadowWeight = 0;
        for (Map.Entry<String, WarManager.WarRole> e : entries) {
            if (e.getValue().official) officialWeight += e.getValue().weight;
            else shadowWeight += e.getValue().weight;
        }

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34};
        for (int i = 0; i < entries.size() && i < slots.length; i++) {
            var e = entries.get(i);
            WarManager.WarRole r = e.getValue();
            gui.setSlot(slots[i], new GuiElementBuilder(
                    r.official ? Items.GOLDEN_HELMET : Items.WITHER_SKELETON_SKULL)
                    .setName(Component.literal(DataManager.getPlayerName(e.getKey()))
                            .withStyle(r.official ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY,
                                    ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7" + r.displayName))
                    .addLoreLine(Component.literal("§7Vote weight: §f" + r.weight))
                    .addLoreLine(Component.literal(r.official ? "§bElected / appointed" : "§8Unelected"))
                    .build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.COMPARATOR)
                .setName(Component.literal("Balance of Power").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Elected / appointed: §b" + officialWeight))
                .addLoreLine(Component.literal("§7Unelected (shadow): §8" + shadowWeight))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Chair §f5§7 + Vice §f4§7 = §b9"))
                .addLoreLine(Component.literal("§7A full shadow bench = §8" + (4 * WarManager.WarRole.SHADOW.weight)))
                .addLoreLine(Component.literal("§8The elected pair outweighs the shadows,"))
                .addLoreLine(Component.literal("§8and the Chair's vote against is a veto."))
                .build());

        gui.setSlot(45, back(() -> open(player)));
        gui.open();
    }

    // ── Shadow view ──

    private static void openShadow(ServerPlayer player, Settlement polity) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x5, player, false);
        gui.setTitle(Component.literal("Shadow Government").withStyle(ChatFormatting.DARK_GRAY));
        fill(gui, 45);

        List<ShadowGovernmentManager.ShadowMember> roster =
                ShadowGovernmentManager.rosterOf(polity.id);

        if (roster.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.COBWEB)
                    .setName(Component.literal("No shadow power").withStyle(ChatFormatting.DARK_GRAY))
                    .addLoreLine(Component.literal("§7Influence grows from wealth,"))
                    .addLoreLine(Component.literal("§7party rank and past office."))
                    .build());
        }

        int[] slots = {11, 13, 15, 21};
        for (int i = 0; i < roster.size() && i < slots.length; i++) {
            var m = roster.get(i);
            gui.setSlot(slots[i], new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                    .setName(Component.literal(m.seat().displayName).withStyle(m.seat().color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§f" + DataManager.getPlayerName(m.uuid())))
                    .addLoreLine(Component.literal("§8" + m.seat().flavor))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Influence: §f" + m.influence()))
                    .addLoreLine(Component.literal("§7War votes: §f" + WarManager.WarRole.SHADOW.weight))
                    .build());
        }

        gui.setSlot(31, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("How shadow power works").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("§7Seats are §onot appointed§r§7 — they are earned:"))
                .addLoreLine(Component.literal("§8• Financier — 100k+ coins"))
                .addLoreLine(Component.literal("§8• Party Boss — leads the ruling party"))
                .addLoreLine(Component.literal("§8• Former Chair — voted out, still owed favours"))
                .addLoreLine(Component.literal("§8• Spymaster — the intelligence apparatus"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Sitting officials are excluded."))
                .build());

        gui.setSlot(36, back(() -> open(player)));
        gui.open();
    }

    // ── helpers ──

    /**
     * Shared backdrop for every screen in the mod. Draws a framed two tone background: a lighter
     * pane around the outside edge and a dark field inside it, so content placed in the interior
     * reads as sitting inside a window. Every GUI that calls this picks up the same look.
     */
    static void fill(SimpleGui gui, int size) {
        GuiKit.frame(gui, size);
    }

    static eu.pb4.sgui.api.elements.GuiElement back(Runnable r) {
        return new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((a, b, c, d) -> r.run())
                .build();
    }
}
