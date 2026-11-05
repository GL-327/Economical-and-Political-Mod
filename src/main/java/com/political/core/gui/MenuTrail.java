package com.political.core.gui;

import eu.pb4.sgui.api.SguiUtils;
import eu.pb4.sgui.api.gui.GuiLike;
import eu.pb4.sgui.api.gui.SimpleGui;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * The menus a player has walked through, so a menu that has no Back button of its own gets one (M7). The mod has a hundred
 * hand-built menus and many of them were written without a way back. Once a tick this looks at the menu each player has open: when
 * it is a different menu from the last tick's (a different title), the last one goes on that player's trail; when it is a menu already
 * on the trail, the player has gone back and the trail is cut to it; when it has no Back of its own and the trail is not empty,
 * {@link BackSlot} puts a Back on it that re-opens the menu on top of the trail. A menu with its own Back is left alone, a menu opened
 * from a command (empty trail) is a root and gets none, and closing the screen clears the trail.
 *
 * <p>The menu a Back returns to is the instance the player saw, shown again as it was: a list can be a few seconds old, but the
 * buttons on it read the live state when they are clicked, as they always did. Titles are compared with digits blurred ("page 2/5"
 * is the same menu as "page 1/5").</p>
 */
public final class MenuTrail {

    private static final int MAX_DEPTH = 8;
    /** How often a menu that already has its Back is checked again, in case it redraws itself and drops it (ticks). */
    private static final int RECHECK_TICKS = 10;

    private static final class State {
        GuiLike last;
        final Deque<SimpleGui> trail = new ArrayDeque<>();
        int sinceCheck;
    }

    private static final Map<UUID, State> STATES = new HashMap<>();

    private MenuTrail() {
    }

    /** Once a tick, on the server thread. */
    public static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            step(player);
        }
    }

    public static void forget(UUID player) {
        STATES.remove(player);
    }

    private static void step(ServerPlayer player) {
        GuiLike current = SguiUtils.getCurrentGui(player);
        State state = STATES.get(player.getUUID());
        if (state == null) {
            if (current == null) {
                return;
            }
            state = new State();
            STATES.put(player.getUUID(), state);
        }
        if (current == state.last) {
            if (current instanceof SimpleGui gui && ++state.sinceCheck >= RECHECK_TICKS) {
                state.sinceCheck = 0;
                ensureBack(player, state, gui);
            }
            return;
        }
        GuiLike previous = state.last;
        state.last = current;
        state.sinceCheck = 0;
        if (!(current instanceof SimpleGui gui)) {
            state.trail.clear(); // closed, or a screen this does not track (anvil, sign, book)
            if (current == null) {
                STATES.remove(player.getUUID());
            }
            return;
        }
        if (previous instanceof SimpleGui from && from != gui) {
            String fromKey = key(from);
            String key = key(gui);
            if (!fromKey.equals(key)) {
                if (!cutTo(state.trail, key)) {
                    state.trail.push(from);
                    while (state.trail.size() > MAX_DEPTH) {
                        state.trail.removeLast();
                    }
                }
            }
        }
        ensureBack(player, state, gui);
    }

    /** If the trail has an entry for this menu, drops it and everything above it (the player went back) and returns true. */
    private static boolean cutTo(Deque<SimpleGui> trail, String key) {
        boolean found = false;
        for (SimpleGui gui : trail) {
            if (key(gui).equals(key)) {
                found = true;
                break;
            }
        }
        if (!found) {
            return false;
        }
        while (!trail.isEmpty()) {
            SimpleGui top = trail.pop();
            if (key(top).equals(key)) {
                break;
            }
        }
        return true;
    }

    private static void ensureBack(ServerPlayer player, State state, SimpleGui gui) {
        SimpleGui parent = state.trail.peek();
        if (parent == null || parent == gui || hasBack(gui) || isDialog(gui)) {
            return;
        }
        BackSlot.add(gui, () -> {
            if (parent.getPlayer() == player && !player.hasDisconnected()) {
                parent.open();
            }
        });
    }

    /** True when some slot already leads back: a button named Back, Return or with a left arrow. */
    static boolean hasBack(SimpleGui gui) {
        int size = gui.getVirtualSize();
        for (int slot = 0; slot < size; slot++) {
            var element = gui.getGuiElement(slot);
            if (element == null) {
                continue;
            }
            String name = element.getItemStack().getHoverName().getString().toLowerCase(Locale.ROOT);
            if (name.contains("back") || name.contains("←") || name.startsWith("return") || name.contains("previous menu")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDialog(SimpleGui gui) {
        String title = gui.getTitle().getString().toLowerCase(Locale.ROOT);
        return title.contains("confirm") || title.endsWith("?");
    }

    private static String key(SimpleGui gui) {
        return gui.getTitle().getString().replaceAll("\\d+", "#");
    }
}
