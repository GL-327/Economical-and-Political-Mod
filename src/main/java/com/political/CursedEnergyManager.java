package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
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

/**
 * V6.2 — the Cursed Energy economy and Curse Hunter progression.
 *
 * <p>Cursed Energy is a NEW currency, kept entirely separate from coins: killing spirits
 * never mints coins, and coins can never be turned into Cursed Energy. Balances, lifetime
 * kills, per-grade tallies and the set of encountered archetypes persist per player in
 * {@code curse_energy.json}.</p>
 *
 * <p>Lifetime spirits slain unlock a Curse Hunter rank (Grade 4 Hunter up to Special Grade
 * Hunter). The rank grants a SMALL passive bonus when fighting spirits, capped at +15%.</p>
 */
public final class CursedEnergyManager {

    private CursedEnergyManager() {}

    /** Curse Hunter ranks, unlocked by lifetime spirits slain. */
    public enum HunterRank {
        GRADE_4_HUNTER("Grade 4 Hunter",     0,     0),
        GRADE_3_HUNTER("Grade 3 Hunter",     25,    3),
        GRADE_2_HUNTER("Grade 2 Hunter",     75,    6),
        GRADE_1_HUNTER("Grade 1 Hunter",     200,  10),
        SPECIAL_GRADE_HUNTER("Special Grade Hunter", 500, 15);

        public final String label;
        public final int killsRequired;
        /** Passive bonus percent vs spirits at this rank (already capped at 15). */
        public final int bonusPercent;

        HunterRank(String label, int killsRequired, int bonusPercent) {
            this.label = label;
            this.killsRequired = killsRequired;
            this.bonusPercent = bonusPercent;
        }

        public static HunterRank forKills(int lifetimeSlain) {
            HunterRank best = GRADE_4_HUNTER;
            for (HunterRank r : values()) {
                if (lifetimeSlain >= r.killsRequired) best = r;
            }
            return best;
        }

        public HunterRank next() {
            int i = ordinal() + 1;
            return i < values().length ? values()[i] : null;
        }
    }

    // ── Per-player record ────────────────────────────────────────────────────
    private static final class Account {
        long energy = 0L;
        long lifetimeEarned = 0L;
        int lifetimeSlain = 0;
        Map<String, Integer> gradeKills = new HashMap<>();
        Set<String> encountered = new HashSet<>();
    }

    private static final Map<String, Account> ACCOUNTS = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path file;

    // ── Persistence ──────────────────────────────────────────────────────────
    public static void initialize(Path worldRoot) {
        file = worldRoot.resolve("curse_energy.json");
        ACCOUNTS.clear();
        if (Files.exists(file)) {
            try (Reader r = Files.newBufferedReader(file)) {
                Map<String, Account> loaded = GSON.fromJson(r,
                        new TypeToken<Map<String, Account>>() {}.getType());
                if (loaded != null) {
                    for (Map.Entry<String, Account> e : loaded.entrySet()) {
                        Account a = e.getValue();
                        if (a == null) continue;
                        if (a.gradeKills == null) a.gradeKills = new HashMap<>();
                        if (a.encountered == null) a.encountered = new HashSet<>();
                        ACCOUNTS.put(e.getKey(), a);
                    }
                }
            } catch (Exception ex) {
                PoliticalServer.LOGGER.warn("Failed to load curse_energy.json", ex);
            }
        }
    }

    public static void save() {
        if (file == null) return;
        try (Writer w = Files.newBufferedWriter(file)) {
            GSON.toJson(ACCOUNTS, w);
        } catch (Exception ex) {
            PoliticalServer.LOGGER.warn("Failed to save curse_energy.json", ex);
        }
    }

    private static Account get(String uuid) {
        return ACCOUNTS.computeIfAbsent(uuid, k -> new Account());
    }

    // ── Currency ───────────────────────────────────────────────────────────────
    public static long getEnergy(String uuid) {
        return get(uuid).energy;
    }

    public static long getEnergy(ServerPlayer player) {
        return getEnergy(player.getUUID().toString());
    }

    /** Adds Cursed Energy (never coins). Returns the new balance. */
    public static long earn(ServerPlayer player, long amount) {
        if (amount <= 0) return getEnergy(player);
        Account a = get(player.getUUID().toString());
        a.energy += amount;
        a.lifetimeEarned += amount;
        save();
        return a.energy;
    }

    public static boolean canAfford(ServerPlayer player, long amount) {
        return getEnergy(player) >= amount;
    }

    /** Spends Cursed Energy if the player can afford it. */
    public static boolean spend(ServerPlayer player, long amount) {
        if (amount <= 0) return true;
        Account a = get(player.getUUID().toString());
        if (a.energy < amount) return false;
        a.energy -= amount;
        save();
        return true;
    }

    // ── Progression ─────────────────────────────────────────────────────────────
    /** Records a spirit kill: bumps lifetime totals and the per-grade tally. */
    public static void recordKill(ServerPlayer player, CursedGrade grade) {
        Account a = get(player.getUUID().toString());
        a.lifetimeSlain++;
        a.gradeKills.merge(grade.name(), 1, Integer::sum);
        save();
    }

    /** Flags an archetype as seen so the codex can reveal it. Returns true if newly discovered. */
    public static boolean markEncountered(ServerPlayer player, String archId) {
        if (archId == null) return false;
        Account a = get(player.getUUID().toString());
        boolean added = a.encountered.add(archId);
        if (added) save();
        return added;
    }

    public static boolean hasEncountered(String uuid, String archId) {
        return get(uuid).encountered.contains(archId);
    }

    public static int lifetimeSlain(String uuid) {
        return get(uuid).lifetimeSlain;
    }

    public static long lifetimeEarned(String uuid) {
        return get(uuid).lifetimeEarned;
    }

    public static int gradeKills(String uuid, CursedGrade grade) {
        return get(uuid).gradeKills.getOrDefault(grade.name(), 0);
    }

    public static HunterRank rank(String uuid) {
        return HunterRank.forKills(get(uuid).lifetimeSlain);
    }

    /** Passive combat/reward bonus vs spirits as a fraction (0.00 .. 0.15). */
    public static double passiveBonus(String uuid) {
        return Math.min(0.15, rank(uuid).bonusPercent / 100.0);
    }

    /** Kills still needed to reach the next rank, or 0 if maxed. */
    public static int killsToNextRank(String uuid) {
        HunterRank cur = rank(uuid);
        HunterRank nxt = cur.next();
        if (nxt == null) return 0;
        return Math.max(0, nxt.killsRequired - get(uuid).lifetimeSlain);
    }

    /** Distinct archetypes this player has encountered. */
    public static List<String> encounteredList(String uuid) {
        return new ArrayList<>(get(uuid).encountered);
    }
}
