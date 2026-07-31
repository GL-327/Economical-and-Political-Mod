package com.political;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/**
 * The incursion board.
 *
 * <p>Two things worth knowing before you open a rift: what the ground you are standing on is about to
 * turn into, and what is already running. Both are on the first screen, because an incursion is
 * decided by where you start it and there is no way to change your mind afterwards.</p>
 */
public final class IncursionGui {

    private IncursionGui() {}

    private static final int SIZE = 54;

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Incursions")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE, GuiKit.DANGER_EDGE);

        IncursionManager.Active mine = IncursionManager.activeFor(player);

        GuiKit.header(gui, Items.END_CRYSTAL, "Incursions", ChatFormatting.DARK_PURPLE,
                "A rift becomes whatever the ground deserves",
                IncursionManager.count() + " running on this server");

        // What would open right here, right now.
        IncursionTheme here = themeHere(player);
        gui.setSlot(20, GuiKit.info(here.icon, "Here it would be", here.colour,
                here.display,
                here.conditions,
                "Hazard: " + here.hazard.label,
                "",
                "Move somewhere else and this changes."));

        gui.setSlot(22, GuiKit.info(Items.WRITABLE_BOOK, "How it works", ChatFormatting.WHITE,
                "Seven waves, each harder than the last.",
                "The final wave carries a sovereign.",
                "The biome under you picks the roster,",
                "the hazard and the boss.",
                "",
                "Leave the ring and it collapses."));

        long cd = IncursionManager.cooldownMinutes(player);
        if (mine == null && cd > 0) {
            gui.setSlot(24, GuiKit.info(Items.CLOCK, "The air has not settled",
                    ChatFormatting.GRAY,
                    "About " + cd + (cd == 1 ? " minute" : " minutes") + " to go.",
                    "",
                    "A rift is an event rather than a job,",
                    "so there is a wait between them."));
        } else if (mine == null) {
            gui.setSlot(24, GuiKit.action(Items.NETHER_STAR, "Open a rift", ChatFormatting.LIGHT_PURPLE,
                    () -> GuiKit.confirm(player, "Open " + here.display,
                            "Seven waves, starting immediately, where you are standing.",
                            () -> {
                                IncursionManager.open(player);
                                player.closeContainer();
                            },
                            () -> open(player)),
                    "Opens where you stand",
                    "You cannot have two at once"));
        } else {
            gui.setSlot(24, GuiKit.info(Items.CLOCK, "One is already running", ChatFormatting.GOLD,
                    mine.theme.display,
                    "Wave " + mine.wave + " of 7",
                    mine.killed + " put down so far",
                    "At " + mine.centre.getX() + ", " + mine.centre.getY()
                            + ", " + mine.centre.getZ()));
        }

        // The full roster of what exists, so a player can go looking for a theme.
        int[] slots = {28, 29, 30, 31, 32, 33, 34, 39, 41};
        IncursionTheme[] all = IncursionTheme.values();
        for (int i = 0; i < all.length && i < slots.length; i++) {
            IncursionTheme t = all[i];
            StringBuilder roster = new StringBuilder();
            for (var e : t.roster) {
                roster.append(e.getDescription().getString()).append(", ");
            }
            String r = roster.length() > 2 ? roster.substring(0, roster.length() - 2) : "";
            gui.setSlot(slots[i], GuiKit.info(t.icon, t.display, t.colour,
                    t.flavor,
                    "",
                    "Hazard: " + t.hazard.label,
                    "Roster: " + r,
                    "Sovereign: " + t.boss.getDescription().getString(),
                    "",
                    t == here ? "§aThis is what opens here" : "§8Found elsewhere"));
        }

        GuiKit.footer(gui, SIZE, player, null);
        gui.open();
    }

    private static IncursionTheme themeHere(ServerPlayer player) {
        try {
            ServerLevel level = (ServerLevel) player.level();
            BlockPos at = player.blockPosition();
            var key = level.getBiome(at).unwrapKey().orElse(null);
            return IncursionTheme.forLocation(
                    level.dimension().identifier().toString(),
                    key == null ? "" : key.identifier().toString(),
                    at.getY());
        } catch (Exception e) {
            return IncursionTheme.BLIGHTED;
        }
    }
}
