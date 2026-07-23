package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The Atlas — a 9x5 world map of every known settlement, plotted by relative world
 * position (north = up), plus a live leadership feed along the bottom row.
 */
public final class AtlasMapGui {

    private AtlasMapGui() {}

    private static final int COLS = 9;
    private static final int ROWS = 4;   // top 4 rows are the map; row 5 is the feed

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x5, player, false);
        gui.setTitle(Component.literal("Atlas — Known Settlements"));

        for (int i = 0; i < 45; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        Collection<Settlement> all = SettlementManager.getAllSettlements();
        List<Settlement> plotted = new ArrayList<>(all);

        if (plotted.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BELL)
                    .setName(Component.literal("No settlements yet")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Place a §eCornerstone §7to found one."))
                    .addLoreLine(Component.literal("§8Craft it, then place it like a bell."))
                    .build());
            addFeed(gui, player);
            gui.open();
            return;
        }

        // World bounds → grid cells.
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (Settlement s : plotted) {
            minX = Math.min(minX, s.cornerstoneX);
            maxX = Math.max(maxX, s.cornerstoneX);
            minZ = Math.min(minZ, s.cornerstoneZ);
            maxZ = Math.max(maxZ, s.cornerstoneZ);
        }
        int spanX = Math.max(1, maxX - minX);
        int spanZ = Math.max(1, maxZ - minZ);

        boolean[] taken = new boolean[COLS * ROWS];

        for (Settlement s : plotted) {
            int col = (int) Math.round((double) (s.cornerstoneX - minX) / spanX * (COLS - 1));
            int row = (int) Math.round((double) (s.cornerstoneZ - minZ) / spanZ * (ROWS - 1));
            int cell = row * COLS + col;
            // Nudge to the next free cell if two settlements collide.
            int guard = 0;
            while (cell < taken.length && taken[cell] && guard++ < taken.length) {
                cell = (cell + 1) % taken.length;
            }
            if (cell >= taken.length) continue;
            taken[cell] = true;

            SettlementTier tier = s.tierEnum();
            GuiElementBuilder b = new GuiElementBuilder(iconFor(tier))
                    .setName(Component.literal(s.name).withStyle(tier.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Tier: " + tier.colorCode + tier.displayName))
                    .addLoreLine(Component.literal("§7Population: §f" + s.population))
                    .addLoreLine(Component.literal("§7Appeal: §f" + s.appeal))
                    .addLoreLine(Component.literal("§7Beds: §f" + s.beds))
                    .addLoreLine(Component.literal("§7Treasury: §6" + s.treasury))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§8" + s.cornerstoneX + ", " + s.cornerstoneZ));

            if (s.leaders != null && !s.leaders.isEmpty()) {
                b.addLoreLine(Component.literal("§7Leaders:"));
                s.leaders.forEach((role, uuid) -> b.addLoreLine(
                        Component.literal("  §7" + role + ": §f" + DataManager.getPlayerName(uuid))));
            }
            if (s.nearestMetropolisId != null) {
                Settlement metro = SettlementManager.get(s.nearestMetropolisId);
                if (metro != null) {
                    b.addLoreLine(Component.literal("§7Follows §6" + metro.name));
                }
            }
            b.addLoreLine(Component.literal(""));
            b.addLoreLine(Component.literal("§eClick for details"));

            final Settlement sel = s;
            b.setCallback((index, clickType, action, slotGui) -> SettlementGui.open(player, sel));
            if (tier.isAtLeast(SettlementTier.METROPOLIS)) b.glow();
            gui.setSlot(cell, b.build());
        }

        addFeed(gui, player);
        gui.open();
    }

    /** Bottom row: live leadership feed. */
    private static void addFeed(SimpleGui gui, ServerPlayer player) {
        for (int i = 36; i < 45; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        String chair = DataManager.getChair();
        String vice = DataManager.getViceChair();

        gui.setSlot(36, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal("Chair").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§f" + (chair == null ? "§8vacant"
                        : DataManager.getPlayerName(chair))))
                .addLoreLine(Component.literal("§8Elected by the largest metropolis"))
                .build());

        gui.setSlot(37, new GuiElementBuilder(Items.IRON_HELMET)
                .setName(Component.literal("Vice Chair").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§f" + (vice == null ? "§8vacant"
                        : DataManager.getPlayerName(vice))))
                .build());

        String prime = HighCouncilManager.getPrimeSeatUuid();
        GuiElementBuilder council = new GuiElementBuilder(Items.BEACON)
                .setName(Component.literal("High Council").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        if (prime == null) {
            council.addLoreLine(Component.literal("§8No council — requires a UTOPIA settlement."));
        } else {
            council.addLoreLine(Component.literal("§7Prime Seat: §f" + DataManager.getPlayerName(prime)));
            HighCouncilManager.getMinisters().forEach((m, uuid) -> council.addLoreLine(
                    Component.literal("§7" + m.displayName + ": §f" + DataManager.getPlayerName(uuid))));
            boolean anyEdict = false;
            for (HighCouncilManager.Edict e : HighCouncilManager.Edict.values()) {
                if (!HighCouncilManager.isEdictActive(e)) continue;
                if (!anyEdict) {
                    council.addLoreLine(Component.literal(""));
                    council.addLoreLine(Component.literal("§7Active edicts:"));
                    anyEdict = true;
                }
                council.addLoreLine(Component.literal("  " + e.color.toString() + e.displayName));
            }
        }
        gui.setSlot(38, council.build());

        Settlement mine = SettlementManager.getRegisteredSettlement(player.getUUID().toString());
        gui.setSlot(44, new GuiElementBuilder(mine == null ? Items.BARRIER : Items.BELL)
                .setName(Component.literal(mine == null ? "Not registered" : "Your settlement: " + mine.name)
                        .withStyle(mine == null ? ChatFormatting.GRAY : ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(mine == null
                        ? "§7Register at a settlement to vote."
                        : "§eClick to open"))
                .setCallback((index, clickType, action, slotGui) -> {
                    if (mine != null) SettlementGui.open(player, mine);
                })
                .build());

        // Hand out a real filled map that renders the actual terrain and blocks.
        gui.setSlot(36, new GuiElementBuilder(Items.FILLED_MAP)
                .setName(Component.literal("Get World Map").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7A real map centred where you stand."))
                .addLoreLine(Component.literal("§7Carry it and walk to reveal the land."))
                .addLoreLine(Component.literal("§8Settlements in view are listed on it."))
                .glow()
                .setCallback((index, clickType, action, slotGui) -> {
                    player.closeContainer();
                    WorldMapItem.give(player);
                })
                .build());
    }

    private static Item iconFor(SettlementTier tier) {
        return switch (tier) {
            case OUTPOST -> Items.OAK_FENCE;
            case HAMLET -> Items.OAK_DOOR;
            case VILLAGE -> Items.OAK_LOG;
            case TOWN -> Items.BRICKS;
            case CITY -> Items.STONE_BRICKS;
            case METROPOLIS -> Items.QUARTZ_BLOCK;
            case MEGALOPOLIS -> Items.GOLD_BLOCK;
            case UTOPIA -> Items.DIAMOND_BLOCK;
            case ELYSIUM -> Items.BEACON;
        };
    }
}
