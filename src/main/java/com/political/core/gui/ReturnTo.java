package com.political.core.gui;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Where a menu's Back button leads (M7). A menu that several other menus open (an admin panel tab, the player hub, a
 * command) cannot know its parent by itself, and its inner screens (a page, a detail view) must keep leading back to
 * its first screen. One {@code ReturnTo} per menu family remembers, per player, the parent the first screen was opened
 * from: the parent opens the menu with {@code openFrom(player, back)}, a command opens it with
 * {@code openFrom(player, null)} (no Back button, the player did not come from a menu), and the menu's own refreshes
 * and inner screens call the plain {@code open(player)} that keeps what was remembered.
 */
public final class ReturnTo {

    private static final Set<ReturnTo> ALL = ConcurrentHashMap.newKeySet();

    private final Map<UUID, Runnable> back = new ConcurrentHashMap<>();

    public ReturnTo() {
        ALL.add(this);
    }

    /** Remembers where this player came from; {@code null} means they did not come from a menu. */
    public void set(UUID player, Runnable parent) {
        if (parent == null) {
            back.remove(player);
        } else {
            back.put(player, parent);
        }
    }

    /** The parent to return to, or {@code null} when there is none (the footer then shows only Close). */
    public Runnable get(UUID player) {
        return back.get(player);
    }

    /** The parent to return to, or {@code fallback} when the screen was not opened from a menu that passed one. */
    public Runnable getOr(UUID player, Runnable fallback) {
        Runnable parent = back.get(player);
        return parent != null ? parent : fallback;
    }

    /** Forgets a player in every menu family; called when they leave so nothing keeps their session alive. */
    public static void forgetEverywhere(UUID player) {
        for (ReturnTo family : ALL) {
            family.back.remove(player);
        }
    }
}
