package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks which title a player is wearing and tells them when they earn a new one.
 *
 * <p>Only the selection is stored. Whether a title is earned is asked of the title itself every
 * time, which means adding a new one needs no migration and a title can never be held after the
 * thing that granted it has gone.</p>
 */
public final class TitleManager {

    private TitleManager() {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path savePath;

    /** player uuid to the title key they are wearing. */
    private static final Map<String, String> WORN = new HashMap<>();
    /** What we have already congratulated each player for, so it announces once. */
    private static final Map<String, Set<String>> SEEN = new HashMap<>();

    private static final class Save {
        Map<String, String> worn = new HashMap<>();
        Map<String, Set<String>> seen = new HashMap<>();
    }

    public static void initialize(Path worldRoot) {
        savePath = worldRoot.resolve("political").resolve("titles.json");
        WORN.clear();
        SEEN.clear();
        if (savePath == null || !Files.exists(savePath)) return;
        try (Reader r = Files.newBufferedReader(savePath)) {
            java.lang.reflect.Type t = new TypeToken<Save>() {}.getType();
            Save data = GSON.fromJson(r, t);
            if (data == null) return;
            if (data.worn != null) WORN.putAll(data.worn);
            if (data.seen != null) SEEN.putAll(data.seen);
        } catch (Exception e) {
            System.out.println("[Titles] Could not read state: " + e.getMessage());
        }
    }

    public static void save() {
        if (savePath == null) return;
        try {
            Files.createDirectories(savePath.getParent());
            Save data = new Save();
            data.worn = new HashMap<>(WORN);
            data.seen = new HashMap<>(SEEN);
            try (Writer w = Files.newBufferedWriter(savePath)) {
                GSON.toJson(data, w);
            }
        } catch (Exception e) {
            System.out.println("[Titles] Could not write state: " + e.getMessage());
        }
    }

    // ── Reading ───────────────────────────────────────────────────────────

    public static Title wornBy(ServerPlayer player) {
        Title t = Title.byKey(WORN.get(player.getUUID().toString()));
        // A title that is no longer earned stops showing, rather than lingering as a claim the
        // player can no longer back up.
        return t != null && t.earnedBy(player) ? t : null;
    }

    public static List<Title> earnedBy(ServerPlayer player) {
        List<Title> out = new ArrayList<>();
        for (Title t : Title.values()) {
            if (t.earnedBy(player)) out.add(t);
        }
        return out;
    }

    public static boolean isWorn(ServerPlayer player, Title t) {
        return t != null && t.key().equals(WORN.get(player.getUUID().toString()));
    }

    // ── Wearing ───────────────────────────────────────────────────────────

    public static boolean wear(ServerPlayer player, Title t) {
        if (t == null) {
            WORN.remove(player.getUUID().toString());
            player.sendSystemMessage(Component.literal("§7Title cleared."));
            save();
            return true;
        }
        if (!t.earnedBy(player)) {
            player.sendSystemMessage(Component.literal("§cYou have not earned that. "
                    + t.requirement));
            return false;
        }
        WORN.put(player.getUUID().toString(), t.key());
        player.sendSystemMessage(Component.literal("§7You are now known as "
                + t.coloured() + "§7."));
        save();
        return true;
    }

    /** How a player's name should read with their title, for chat and menus. */
    public static String decorate(ServerPlayer player) {
        Title t = wornBy(player);
        String name = player.getName().getString();
        return t == null ? name : name + " " + t.coloured();
    }

    // ── Noticing new ones ─────────────────────────────────────────────────

    /**
     * Ticked occasionally. Announces anything newly earned, once.
     *
     * <p>Deliberately infrequent. Every check reaches across several managers, and a title is not
     * time critical, so once every fifteen seconds is plenty and costs nothing noticeable.</p>
     */
    public static void tickPlayer(ServerPlayer player) {
        if (player.tickCount % 300 != 0) return;
        String uuid = player.getUUID().toString();
        Set<String> seen = SEEN.computeIfAbsent(uuid, k -> new HashSet<>());

        boolean any = false;
        for (Title t : Title.values()) {
            if (seen.contains(t.key())) continue;
            if (!t.earnedBy(player)) continue;
            seen.add(t.key());
            any = true;
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("  §6§lTITLE EARNED"));
            player.sendSystemMessage(Component.literal("  " + t.coloured()));
            player.sendSystemMessage(Component.literal("  §8" + t.requirement));
            player.sendSystemMessage(Component.literal("  §7Wear it with §f/titles"));
            player.sendSystemMessage(Component.literal(""));
        }
        if (any) save();
    }

    public static void clear(UUID uuid) {
        // Titles are earned progress and survive a disconnect.
    }
}
