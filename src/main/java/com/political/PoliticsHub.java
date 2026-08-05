package com.political;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * The front door to the politics, and the answer to a mod that grew past its own foundation.
 *
 * <p>This started as a mod about electing somebody and letting them pick perks. Then it gained
 * settlements, guilds, parties, wars, sieges, clans and a hundred other screens, and the original
 * loop ended up as one command among ninety. A player logging in had no way to find the thing the
 * whole server is supposedly about.</p>
 *
 * <p>So this screen answers four questions in the order somebody actually asks them: who is in
 * charge, when is the next election, what are they doing with the power, and what can I do about
 * it. Everything else is reachable from here but nothing else is required to understand it.</p>
 */
public final class PoliticsHub {

    private PoliticsHub() {}

    private static final int SIZE = 54;

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Politics")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        String phase = safePhase();
        String chair = safeChair();
        List<String> active = safeActivePerks();

        GuiKit.header(gui, Items.GOLDEN_HELMET, "Politics", ChatFormatting.GOLD,
                chair == null ? "Nobody is in charge" : "Led by " + chair,
                phase,
                active.isEmpty() ? "No perks are active" : active.size() + " perks in force");

        // ── 1. Who is in charge ──
        gui.setSlot(19, GuiKit.action(Items.PLAYER_HEAD, "Who is in charge",
                ChatFormatting.YELLOW,
                () -> runCommand(player, "chair"),
                chair == null ? "The seat is empty" : chair,
                "",
                "The Chair picks the perks everybody",
                "on the server then lives with.",
                "",
                "§8That is the whole point of the election."));

        // ── 2. The election ──
        gui.setSlot(21, GuiKit.action(Items.PAPER, "The election", ChatFormatting.AQUA,
                () -> runCommand(player, "election"),
                phase,
                "",
                "Stand for the seat, or vote for",
                "somebody who is standing.",
                "",
                "§8One election decides the perks until",
                "§8the next one."));

        // ── 3. The perks, which are the reason any of it matters ──
        gui.setSlot(23, GuiKit.action(Items.NETHER_STAR, "Perks in force",
                ChatFormatting.LIGHT_PURPLE,
                () -> PerksGui.open(player, isChairOf(player)),
                active.isEmpty() ? "None chosen yet" : active.size() + " active",
                "",
                "Server wide bonuses the Chair chose.",
                "Everybody gets them, whoever they voted for.",
                "",
                "§8Win the seat to change them."));

        // ── 4. What a player can do about it ──
        gui.setSlot(25, GuiKit.action(Items.IRON_SWORD, "Your standing",
                ChatFormatting.GREEN,
                () -> runCommand(player, "party"),
                "Parties, backing and influence",
                "",
                "Join a party, back a candidate, and",
                "build the standing that wins seats."));

        // ── The rest, one row down and clearly secondary ──
        gui.setSlot(37, route(player, Items.OAK_DOOR, "Settlements", ChatFormatting.GREEN,
                "settlement", "Towns, cities and who owns them"));
        gui.setSlot(39, route(player, Items.SHIELD, "Guilds and clans", ChatFormatting.BLUE,
                "guild", "The groups you can belong to"));
        gui.setSlot(41, GuiKit.tile(Items.FILLED_MAP, "Atlas", ChatFormatting.YELLOW,
                () -> {
                    AtlasMap.give(player, AtlasCartographer.Zoom.REGION);
                    player.sendSystemMessage(Component.literal(
                            "§e✦ §7A map of the region. Hang it, copy it, hand it on."));
                },
                "A real map, drawn on paper",
                "§8Not a screen. Carry it."));
        // "wars" was never a command. The real ones are "war" and "sieges".
        gui.setSlot(43, route(player, Items.NETHERITE_SWORD, "Wars", ChatFormatting.RED,
                "war", "What the polities are doing to each other"));
        gui.setSlot(34, route(player, Items.IRON_BARS, "Sieges", ChatFormatting.DARK_RED,
                "sieges", "Who is being besieged right now"));

        gui.setSlot(SIZE - 5, GuiKit.info(Items.WRITABLE_BOOK, "How this works",
                ChatFormatting.WHITE,
                "Four steps, and that is the whole game.",
                "",
                "  1  Somebody stands for the Chair",
                "  2  Everybody votes",
                "  3  The winner picks the perks",
                "  4  The whole server lives with them",
                "",
                "Everything else on this screen is a way",
                "to build the standing that wins step two,",
                "or a place to spend what step three gave",
                "you.",
                "",
                "§8If you only ever use one screen, use",
                "§8this one."));

        GuiKit.footer(gui, SIZE, player, null);
        gui.open();
    }

    // ── Safe readers ──────────────────────────────────────────────────────
    //
    // The hub sits on top of nine systems that each load at their own pace. One of them not being
    // ready must never be the reason the front door will not open, so every read here degrades to
    // a sentence rather than an exception.

    private static String safePhase() {
        try {
            String p = ElectionManager.phaseText();
            return p == null || p.isBlank() ? "No election running" : p;
        } catch (Exception e) {
            return "No election running";
        }
    }

    private static String safeChair() {
        try {
            var data = DataManager.getData();
            if (data == null || data.chair == null || data.chair.isBlank()) return null;
            return data.chair;
        } catch (Exception e) {
            return null;
        }
    }

    /** Whether this player currently holds the seat, which decides if the perk screen is editable. */
    private static boolean isChairOf(ServerPlayer player) {
        try {
            var data = DataManager.getData();
            if (data == null || data.chair == null) return false;
            return data.chair.equalsIgnoreCase(player.getName().getString())
                    || data.chair.equalsIgnoreCase(player.getUUID().toString());
        } catch (Exception e) {
            return false;
        }
    }

    private static List<String> safeActivePerks() {
        try {
            List<String> a = PerkManager.getActivePerks();
            return a == null ? List.of() : a;
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Whether a command is actually registered.
     *
     * <p>The hub routes by name, so a name that does not exist fails at click time with nothing to
     * show for it. Two of the first six were wrong and nothing caught them, because routing by
     * string has no compiler behind it. Checking the dispatcher is the only honest substitute.</p>
     */
    private static boolean exists(String command) {
        try {
            if (PoliticalServer.server == null) return false;
            String root = command.split(" ")[0];
            return PoliticalServer.server.getCommands().getDispatcher()
                    .getRoot().getChild(root) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * A tile that routes to a command, or says plainly that it cannot.
     *
     * <p>Greyed out rather than hidden. A missing screen is a thing worth reporting, and silently
     * dropping it would leave the hub looking complete while being wrong.</p>
     */
    private static eu.pb4.sgui.api.elements.GuiElement route(ServerPlayer player,
            net.minecraft.world.item.Item icon, String label, ChatFormatting colour,
            String command, String... lore) {
        if (exists(command)) {
            return GuiKit.tile(icon, label, colour, () -> runCommand(player, command), lore);
        }
        String[] why = new String[lore.length + 2];
        System.arraycopy(lore, 0, why, 0, lore.length);
        why[lore.length] = "";
        why[lore.length + 1] = "§cNot available on this server.";
        return GuiKit.info(Items.BARRIER, label, ChatFormatting.DARK_GRAY, why);
    }

    /**
     * Runs a command as the player.
     *
     * <p>The hub deliberately routes through commands rather than calling each screen directly.
     * Ninety screens have ninety different opener signatures, several of them take arguments this
     * screen has no way to know, and a hub that had to be updated every time one of them changed
     * would go stale within a week.</p>
     */
    private static void runCommand(ServerPlayer player, String command) {
        try {
            if (PoliticalServer.server == null) return;
            PoliticalServer.server.getCommands().performPrefixedCommand(
                    player.createCommandSourceStack(), command);
        } catch (Exception e) {
            player.sendSystemMessage(Component.literal(
                    "§cThat part is not available right now."));
        }
    }
}
