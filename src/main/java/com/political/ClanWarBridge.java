package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.level.ServerPlayer;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * V7 Pillar A — clan feuds. A small, self-contained mirror of the war pattern (it reads how
 * {@link WarManager} scores kills but edits none of it). Two clans can declare a feud; while it
 * runs, members of the feuding clans hit each other harder, and each fair kill scores a point
 * toward victory. The first clan to the target score wins a burst of prestige and a coin tribute
 * paid out of the loser's treasury (a pure transfer, so it never inflates the economy).
 *
 * <p><b>Anti-abuse.</b> The same victim only scores again for a killer after a cooldown, so a
 * feud cannot be farmed by camping one player. Score comes from real PvP kills between opted-in
 * clans, never from time.</p>
 */
public final class ClanWarBridge {

    private ClanWarBridge() {}

    public static final int TARGET_SCORE = 10;
    public static final long FEUD_WIN_PRESTIGE = 500L;
    public static final long TRIBUTE = 100_000L;
    /** Same killer/victim pair cannot score again within this window. */
    public static final long SAME_VICTIM_COOLDOWN_MS = 120_000L;
    /** Extra damage members of feuding clans deal to one another. */
    public static final float FEUD_DAMAGE_BONUS = 0.15f;

    public static final class Feud {
        public String clanA;
        public String clanB;
        public long scoreA;
        public long scoreB;
        public long startMs;

        public Feud() {}

        public Feud(String clanA, String clanB) {
            this.clanA = clanA;
            this.clanB = clanB;
            this.startMs = System.currentTimeMillis();
        }
    }

    private static final class SaveFile {
        List<Feud> feuds = new ArrayList<>();
    }

    private static final List<Feud> FEUDS = new ArrayList<>();
    /** killerUuid + ">" + victimUuid -> epoch millis the pair may score again. */
    private static final Map<String, Long> VICTIM_COOLDOWN = new ConcurrentHashMap<>();
    private static Path dataFile;

    // ── persistence ──

    public static void initialize(Path worldRoot) {
        dataFile = worldRoot.resolve("clan_feuds.json");
        FEUDS.clear();
        VICTIM_COOLDOWN.clear();
        if (Files.exists(dataFile)) {
            try (Reader r = Files.newBufferedReader(dataFile)) {
                Gson g = new GsonBuilder().create();
                Type t = new TypeToken<SaveFile>() {}.getType();
                SaveFile sf = g.fromJson(r, t);
                if (sf != null && sf.feuds != null) {
                    for (Feud f : sf.feuds) {
                        if (f != null && f.clanA != null && f.clanB != null) FEUDS.add(f);
                    }
                }
            } catch (Exception e) {
                System.err.println("[ClanWarBridge] Failed to load: " + e.getMessage());
            }
        }
    }

    public static void save() {
        if (dataFile == null) return;
        try {
            Files.createDirectories(dataFile.getParent());
            SaveFile sf = new SaveFile();
            sf.feuds = new ArrayList<>(FEUDS);
            Gson g = new GsonBuilder().setPrettyPrinting().create();
            try (Writer w = Files.newBufferedWriter(dataFile)) {
                g.toJson(sf, w);
            }
        } catch (Exception e) {
            System.err.println("[ClanWarBridge] Failed to save: " + e.getMessage());
        }
    }

    // ── queries ──

    public static List<Feud> activeFeuds() {
        return new ArrayList<>(FEUDS);
    }

    public static Feud feudBetween(String clanIdA, String clanIdB) {
        if (clanIdA == null || clanIdB == null) return null;
        for (Feud f : FEUDS) {
            if ((f.clanA.equals(clanIdA) && f.clanB.equals(clanIdB))
                    || (f.clanA.equals(clanIdB) && f.clanB.equals(clanIdA))) {
                return f;
            }
        }
        return null;
    }

    public static boolean areFeuding(String clanIdA, String clanIdB) {
        return feudBetween(clanIdA, clanIdB) != null;
    }

    /** Feuds this clan is currently part of. */
    public static List<Feud> feudsOf(String clanId) {
        List<Feud> out = new ArrayList<>();
        if (clanId == null) return out;
        for (Feud f : FEUDS) {
            if (f.clanA.equals(clanId) || f.clanB.equals(clanId)) out.add(f);
        }
        return out;
    }

    // ── lifecycle ──

    /** Start a feud between two clans. Returns false if one already runs or the clans are equal. */
    public static boolean declareFeud(SorcererClan a, SorcererClan b) {
        if (a == null || b == null || a.id.equals(b.id)) return false;
        if (feudBetween(a.id, b.id) != null) return false;
        FEUDS.add(new Feud(a.id, b.id));
        a.rivalSet().add(b.id);
        b.rivalSet().add(a.id);
        a.allySet().remove(b.id);
        b.allySet().remove(a.id);
        ClanManager.logTo(a, "Declared a feud against " + b.name + ".");
        ClanManager.logTo(b, a.name + " has declared a feud against your clan.");
        ClanManager.broadcastClan(a, "§c⚔ Your clan is now feuding with §f" + b.name + "§c.");
        ClanManager.broadcastClan(b, "§c⚔ §f" + a.name + " §chas declared a feud against your clan.");
        ClanManager.saveAll();
        save();
        return true;
    }

    /** End a feud with no winner (both Heads can walk away via the GUI). */
    public static boolean endFeud(SorcererClan a, SorcererClan b) {
        Feud f = feudBetween(a.id, b.id);
        if (f == null) return false;
        FEUDS.remove(f);
        a.rivalSet().remove(b.id);
        b.rivalSet().remove(a.id);
        ClanManager.logTo(a, "The feud with " + b.name + " has ended.");
        ClanManager.logTo(b, "The feud with " + a.name + " has ended.");
        ClanManager.saveAll();
        save();
        return true;
    }

    // ── scoring ──

    /** Called when one player kills another; scores the feud if their clans are feuding. */
    public static void onPlayerKill(ServerPlayer killer, ServerPlayer victim) {
        if (killer == null || victim == null || killer == victim) return;
        SorcererClan kc = ClanManager.getClanOf(killer.getUUID().toString());
        SorcererClan vc = ClanManager.getClanOf(victim.getUUID().toString());
        if (kc == null || vc == null || kc.id.equals(vc.id)) return;
        Feud f = feudBetween(kc.id, vc.id);
        if (f == null) return;

        // Anti-farm: the same victim only scores again after a cooldown.
        String key = killer.getUUID() + ">" + victim.getUUID();
        long now = System.currentTimeMillis();
        Long until = VICTIM_COOLDOWN.get(key);
        if (until != null && now < until) return;
        VICTIM_COOLDOWN.put(key, now + SAME_VICTIM_COOLDOWN_MS);

        if (f.clanA.equals(kc.id)) f.scoreA++;
        else f.scoreB++;

        ClanManager.addPrestige(kc, 20L, "feud kill");
        ClanManager.broadcastClan(kc, "§a⚔ Feud point vs §f" + vc.name + "§a. Score §f"
                + scoreFor(f, kc.id) + "§7/§f" + TARGET_SCORE);
        ClanManager.broadcastClan(vc, "§c⚔ §f" + killer.getName().getString()
                + " §cscored against your clan. §7Score §f" + scoreFor(f, vc.id) + "§7/§f" + TARGET_SCORE);

        long ks = scoreFor(f, kc.id);
        if (ks >= TARGET_SCORE) {
            resolve(f, kc, vc);
        }
        save();
    }

    private static long scoreFor(Feud f, String clanId) {
        return f.clanA.equals(clanId) ? f.scoreA : f.scoreB;
    }

    private static void resolve(Feud f, SorcererClan winner, SorcererClan loser) {
        FEUDS.remove(f);
        winner.rivalSet().remove(loser.id);
        loser.rivalSet().remove(winner.id);

        ClanManager.addPrestige(winner, FEUD_WIN_PRESTIGE, "feud victory");

        long tribute = Math.min(TRIBUTE, Math.max(0L, loser.treasury));
        if (tribute > 0) {
            loser.treasury -= tribute;
            winner.treasury += tribute;
        }

        ClanManager.logTo(winner, "Won the feud against " + loser.name + " (+"
                + FEUD_WIN_PRESTIGE + " prestige, +" + ClanManager.fmt(tribute) + " tribute).");
        ClanManager.logTo(loser, "Lost the feud against " + winner.name + " (-"
                + ClanManager.fmt(tribute) + " tribute).");
        ClanManager.broadcastClan(winner, "§6✦ Your clan won the feud against §f" + loser.name
                + "§6! §7+" + FEUD_WIN_PRESTIGE + " prestige, +" + ClanManager.fmt(tribute) + " coins.");
        ClanManager.broadcastClan(loser, "§c✦ Your clan lost the feud against §f" + winner.name
                + "§c. §7-" + ClanManager.fmt(tribute) + " coins to tribute.");
        ClanManager.saveAll();
    }

    /** Purge feuds that reference a clan that no longer exists (call after a disband). */
    public static void purgeClan(String clanId) {
        if (clanId == null) return;
        FEUDS.removeIf(f -> f.clanA.equals(clanId) || f.clanB.equals(clanId));
        save();
    }
}
