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

/**
 * The way in.
 *
 * <p>One screen listing the seven floors in order, with the ones you have not earned shown but
 * locked rather than hidden. Seeing the floor above the one you are on is most of the reason to
 * clear the one you are on.</p>
 */
public final class DungeonGui {

    private DungeonGui() {}

    private static final int SIZE = 54;

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Dungeons")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        int cleared = DungeonManager.progressOf(player);
        DungeonRun active = DungeonManager.runOf(player);

        GuiKit.header(gui, Items.SKELETON_SKULL, "Dungeons", ChatFormatting.DARK_PURPLE,
                cleared == 0 ? "You have cleared nothing yet"
                        : "Cleared up to floor " + cleared,
                DungeonCatalog.count() + " rooms across " + DungeonFloor.values().length + " floors",
                DungeonManager.activeRuns() + " parties inside right now");

        // The seven floors, in order, along the middle two rows.
        int[] slots = {19, 20, 21, 22, 23, 24, 25};
        DungeonFloor[] floors = DungeonFloor.values();
        for (int i = 0; i < floors.length && i < slots.length; i++) {
            final DungeonFloor floor = floors[i];
            boolean open = DungeonManager.canEnter(player, floor);
            boolean done = cleared >= floor.tier;
            String best = DungeonManager.bestScore(player, floor);
            int rooms = DungeonCatalog.countFor(floor);

            GuiElementBuilder b = new GuiElementBuilder(open ? floor.icon : Items.IRON_BARS)
                    .setName(Component.literal(floor.tier + ". " + floor.display)
                            .withStyle(open ? floor.colour : ChatFormatting.DARK_GRAY,
                                    ChatFormatting.BOLD))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§8" + floor.flavour))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Rooms drawn from §f" + rooms))
                    .addLoreLine(Component.literal("§7Laid out §f" + floor.roomCount()
                            + " §7at a time"))
                    .addLoreLine(Component.literal("§7Secrets §f" + floor.secretCount()))
                    .addLoreLine(Component.literal("§7Boss §f" + floor.bossName))
                    .addLoreLine(Component.literal(""));

            if (best != null) {
                b.addLoreLine(Component.literal("§7Your best §f" + best));
            }
            if (done) {
                b.addLoreLine(Component.literal("§aCleared."));
                b.glow();
            }

            if (!open) {
                DungeonFloor prev = floor.previous();
                b.addLoreLine(Component.literal("§cLocked. Clear "
                        + (prev == null ? "the floor below" : prev.display) + " first."));
            } else if (active != null) {
                b.addLoreLine(Component.literal("§cYou are already in a run."));
            } else if (rooms < 4) {
                b.addLoreLine(Component.literal("§cNo rooms installed for this floor."));
            } else {
                b.addLoreLine(Component.literal("§eClick to go in."));
            }

            gui.setSlot(slots[i], b.setCallback((s, t, ci, sg) -> {
                if (!DungeonManager.canEnter(player, floor)) return;
                List<ServerPlayer> party = new ArrayList<>();
                party.add(player);
                String refusal = DungeonManager.start(player, floor, party);
                if (refusal != null) {
                    player.sendSystemMessage(Component.literal("§c" + refusal));
                }
            }).build());
        }

        if (active != null) {
            gui.setSlot(40, GuiKit.action(Items.BARRIER, "Leave the run", ChatFormatting.RED,
                    () -> {
                        DungeonManager.leave(player, false);
                        open(player);
                    },
                    "You are in " + active.floor.display,
                    active.roomsCleared + "/" + active.roomsTotal + " rooms cleared",
                    "",
                    "Leaving abandons the run. Nothing is kept."));
        }

        gui.setSlot(SIZE - 5, GuiKit.info(Items.WRITABLE_BOOK, "How a run is scored",
                ChatFormatting.WHITE,
                "Out of three hundred, across four parts.",
                "",
                "  Explore  100  every secret you find",
                "  Skill    100  rooms cleared, minus deaths",
                "  Speed     60  par is three minutes a tier",
                "  Bonus     40  no deaths, all secrets, boss",
                "",
                "Finding things beats killing things, and",
                "dying costs more than being slow.",
                "",
                "§8S+ pays three times what D does."));

        gui.setSlot(SIZE - 6, GuiKit.info(Items.CHEST, "What is in there",
                ChatFormatting.GRAY,
                "Rooms are drawn fresh every run, so no",
                "two visits to a floor lay out the same.",
                "",
                "Almost nothing is breakable. The route is",
                "the route, and a secret has to be found",
                "rather than mined through.",
                "",
                "§8Builds from Custom Dungeons, Underground",
                "§8Worlds and Dungeons and Taverns."));

        GuiKit.footer(gui, SIZE, player, null);
        gui.open();
    }
}
