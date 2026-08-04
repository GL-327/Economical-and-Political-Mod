package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The cartographer's table.
 *
 * <p>This screen used to <em>be</em> the Atlas: forty five glass panes arranged by world position,
 * which is a spreadsheet wearing a map's coat. The Atlas is now a real drawn map you hold, so all
 * this screen does is hand one out at the zoom you want, redraw the ones you already carry, and
 * list the settlements so you can find one by name rather than by squinting.</p>
 */
public final class AtlasMapGui {

    private AtlasMapGui() {}

    private static final int SIZE = 54;

    public static void open(ServerPlayer player) {
        open(player, 0);
    }

    public static void open(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Cartographer's Table")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        List<Settlement> all = new ArrayList<>(SettlementManager.getAllSettlements());
        all.sort(Comparator.comparingInt((Settlement s) -> -s.tierEnum().rank())
                .thenComparing(s -> s.name == null ? "" : s.name));

        GuiKit.header(gui, Items.FILLED_MAP, "Cartographer's Table", ChatFormatting.GOLD,
                all.size() + " settlements on record",
                "Take a sheet, then read it in your hand");

        // ── The three sheets ──────────────────────────────────────────────
        int[] sheetSlots = {11, 13, 15};
        AtlasCartographer.Zoom[] zooms = AtlasCartographer.Zoom.values();
        for (int i = 0; i < zooms.length && i < sheetSlots.length; i++) {
            final AtlasCartographer.Zoom zoom = zooms[i];
            gui.setSlot(sheetSlots[i], new GuiElementBuilder(Items.FILLED_MAP)
                    .setName(Component.literal(zoom.display)
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§8" + zoom.blurb))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Covers §f" + zoom.span() + "§7 blocks across"))
                    .addLoreLine(Component.literal("§7Centred where you stand"))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Terrain, claims, roads, wars and names,"))
                    .addLoreLine(Component.literal("§7drawn onto the paper itself."))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to have one drawn"))
                    .glow()
                    .setCallback((a, b, c, d) -> {
                        player.closeContainer();
                        AtlasMap.give(player, zoom);
                    })
                    .build());
        }

        gui.setSlot(29, GuiKit.action(Items.INK_SAC, "Redraw what I carry",
                ChatFormatting.AQUA,
                () -> {
                    int n = AtlasMap.refreshHeld(player);
                    player.sendSystemMessage(Component.literal(n == 0
                            ? "§7You are not carrying an atlas. Take one above."
                            : "§a" + n + " sheet" + (n == 1 ? "" : "s") + " brought up to date."));
                    open(player, page);
                },
                "Every atlas in your pack is repainted",
                "with the world as it stands now.",
                "",
                "Sheets keep their identity, so a copy",
                "hanging on a wall updates with it."));

        gui.setSlot(33, GuiKit.action(Items.COMPASS, "Plain explorer's map",
                ChatFormatting.GRAY,
                () -> {
                    player.closeContainer();
                    WorldMapItem.give(player);
                },
                "A vanilla map that fills itself in",
                "as you walk, with no politics on it.",
                "",
                "Use this one for finding your way home."));

        // ── The gazetteer ─────────────────────────────────────────────────
        int perPage = 7;
        int from = Math.max(0, page) * perPage;
        int[] listSlots = {37, 38, 39, 40, 41, 42, 43};
        for (int i = 0; i < perPage; i++) {
            int idx = from + i;
            if (idx >= all.size()) break;
            Settlement s = all.get(idx);
            SettlementTier tier = s.tierEnum();

            GuiElementBuilder b = new GuiElementBuilder(iconFor(tier))
                    .setName(Component.literal(s.name).withStyle(tier.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7" + tier.colorCode + tier.displayName
                            + " §8· §7pop §f" + s.population + " §8· §7appeal §f" + s.appeal))
                    .addLoreLine(Component.literal("§8" + s.cornerstoneX + ", " + s.cornerstoneZ
                            + " §8in " + shortDim(s.worldDim)))
                    .addLoreLine(Component.literal(""));

            if (s.leaders != null && !s.leaders.isEmpty()) {
                s.leaders.forEach((role, uuid) -> b.addLoreLine(
                        Component.literal("§7" + role + ": §f" + DataManager.getPlayerName(uuid))));
                b.addLoreLine(Component.literal(""));
            }
            b.addLoreLine(Component.literal("§eLeft click §7for the full record"));
            b.addLoreLine(Component.literal("§eRight click §7to draw a sheet centred here"));

            if (tier.isAtLeast(SettlementTier.METROPOLIS)) b.glow();

            b.setCallback((slot, clickType, action, sg) -> {
                if (clickType.isRight) {
                    player.closeContainer();
                    ServerLevel level = (ServerLevel) player.level();
                    var stack = AtlasMap.issue(level, s.cornerstoneX, s.cornerstoneZ,
                            AtlasCartographer.Zoom.LOCAL, player);
                    if (!player.getInventory().add(stack)) player.drop(stack, false);
                    player.sendSystemMessage(Component.literal(
                            "§6✦ §7A local chart of §f" + s.name + "§7 is drawn."));
                } else {
                    SettlementGui.open(player, s);
                }
            });
            gui.setSlot(listSlots[i], b.build());
        }

        if (all.isEmpty()) {
            gui.setSlot(40, GuiKit.info(Items.BELL, "Nothing founded yet", ChatFormatting.GRAY,
                    "Place a Cornerstone to found a settlement.",
                    "It will appear on every atlas from then on."));
        }

        GuiKit.pager(gui, SIZE, page, all.size(), perPage, p -> open(player, p));
        GuiKit.footer(gui, SIZE, player, () -> CivilizationHubGui.open(player));
        gui.open();
    }

    private static String shortDim(String dim) {
        if (dim == null) return "?";
        int colon = dim.indexOf(':');
        return colon < 0 ? dim : dim.substring(colon + 1);
    }

    private static net.minecraft.world.item.Item iconFor(SettlementTier tier) {
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
