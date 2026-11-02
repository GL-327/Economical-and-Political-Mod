package com.political.settlement;
import com.political.VarietyGui;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * V6 variety — a rotating settlement-event system.
 *
 * <p>Every so often a random eligible settlement gets a flavourful, time-limited event — a
 * festival, a plague, a gold rush, a bandit scare — that temporarily shifts its
 * happiness / appeal / food / treasury and is announced to nearby players. Active events persist
 * across restart and are fully inspectable in {@link SettlementEventGui} / {@link VarietyGui}.</p>
 *
 * <h2>How the shift is applied without editing the economy</h2>
 * {@link SettlementEconomy} recomputes happiness/food every cycle and {@link SettlementScanner}
 * recomputes appeal on rescan, so this class does NOT try to hold a value in those fields forever.
 * Instead it re-applies its delta with an <b>apply-once-until-changed</b> guard: it remembers the
 * exact value it last wrote; if the field still equals that value it leaves it alone, and only when
 * something else (the economy / a scan) has overwritten the field back to a fresh base does it
 * re-apply the delta once. This can never runaway-stack, and when an event ends we simply stop
 * re-applying — the field decays back to its true base within one economy cycle (≤60s). No existing
 * file is touched.
 *
 * <h2>Why it cannot be farmed</h2>
 * Events are started only by this class's own wall-clock rotation timer — never by any player
 * action, so there is nothing to spam. All magnitudes are hard-capped constants, at most one event
 * runs per settlement, events never stack, income events add a small per-cycle amount under a hard
 * ceiling, and one-time treasury effects are a capped fraction. Nothing here pays a player's wallet.
 */
public final class SettlementEvents {

    private SettlementEvents() {}

    // ── Tunables (all caps live here) ────────────────────────────────────────

    /** How often we consider starting a new event somewhere. */
    private static final long ROTATE_INTERVAL_MS = 8L * 60L * 1000L;     // every 8 real minutes
    /** Reapply cadence (ticks) — cheap, and comfortably faster than an economy cycle. */
    private static final int APPLY_INTERVAL_TICKS = 20;
    /** Income events bank at most this many coins into a treasury per economy cycle. */
    private static final long MAX_INCOME_PER_CYCLE = 400L;
    /** One-time treasury effects move at most this fraction / this absolute cap. */
    private static final double ONE_TIME_FRACTION = 0.06;
    private static final long ONE_TIME_ABS_CAP = 5_000L;
    /** Never run more than this many events at once, regardless of settlement count. */
    private static final int MAX_CONCURRENT = 4;
    /** Chance, per rotation tick, that we actually fire a new event (keeps them special). */
    private static final double FIRE_CHANCE = 0.75;

    private static final int INCOME_CYCLE_TICKS = SettlementEconomy.CYCLE_TICKS;

    // ── Event catalogue ──────────────────────────────────────────────────────

    /**
     * A settlement event. {@code durationMin} is real minutes. The deltas are the whole effect:
     * happiness/appeal/food are re-applied via the apply-once guard; incomePerCycle is banked to the
     * treasury on the income timer; oneTimeTreasury is a signed capped shock at start.
     */
    public enum Type {
        FESTIVAL          ("Festival",          "§d", Items.CAKE,             20, +15, +10,  0, 0, false, true,  "The whole settlement downs tools to celebrate."),
        GOOD_HARVEST      ("Good Harvest",      "§a", Items.WHEAT,            15,  +5,   0, +8, 0, false, false, "Barns overflow and bellies are full."),
        GOLD_RUSH         ("Gold Rush",         "§6", Items.RAW_GOLD,         20,  +8,  +5,  0, 300, false, false, "Word of riches draws fortune-seekers."),
        PLAGUE            ("Plague",            "§2", Items.POISONOUS_POTATO,  15, -20,   0, -4, 0, false, false, "Sickness spreads through the crowded streets."),
        BANDIT_SCARE      ("Bandit Scare",      "§c", Items.CROSSBOW,         12, -10,   0,  0, -1, true, false, "Outlaws on the roads; the treasury is raided."),
        MIGRATION_WAVE    ("Migration Wave",    "§b", Items.LEATHER_BOOTS,    18,  +5, +12,  0, 0, false, false, "Newcomers arrive looking for a home."),
        MARKET_DAY        ("Market Day",        "§e", Items.EMERALD,          15,  +6,   0,  0, 250, false, false, "Traders pack the square; coin changes hands."),
        DROUGHT           ("Drought",           "§6", Items.DEAD_BUSH,        18,  -8,   0, -6, 0, false, false, "The wells run low and the fields crack."),
        RELIGIOUS_REVIVAL ("Religious Revival", "§f", Items.TOTEM_OF_UNDYING,16, +12,  +6,  0, 0, false, true,  "A wave of faith lifts every spirit."),
        BOOM_TOWN         ("Boom Town",         "§6", Items.GOLD_BLOCK,       20,   0, +15, 0, 200, false, false, "Building never stops; the skyline grows."),
        MOURNING          ("Period of Mourning","§8", Items.WITHER_ROSE,      10, -12,   0,  0, 0, false, false, "A great figure has passed; flags fly low."),
        CARNIVAL          ("Carnival",          "§d", Items.FIREWORK_ROCKET,  14, +18,  +8,  0, 0, false, true,  "Masks, music and colour fill the streets."),
        BUMPER_CROP       ("Bumper Crop",       "§a", Items.HAY_BLOCK,        12,   0,   0, +10, 0, false, false, "A once-in-a-decade growing season."),
        TAX_UNREST        ("Tax Unrest",        "§c", Items.IRON_BARS,        12, -15,   0,  0, -1, true, false, "Grumbling over levies turns to open anger.");

        public final String displayName;
        public final String colorCode;
        public final Item icon;
        public final int durationMin;
        public final int happinessDelta;
        public final int appealDelta;
        public final int foodDelta;
        /** >0 = coins/cycle to treasury; -1 = fires a one-time treasury theft at start; 0 = none. */
        public final int incomeCode;
        public final boolean oneTimeTheft;
        public final boolean playerBuff;
        public final String flavour;

        Type(String displayName, String colorCode, Item icon, int durationMin,
             int happinessDelta, int appealDelta, int foodDelta, int incomeCode,
             boolean oneTimeTheft, boolean playerBuff, String flavour) {
            this.displayName = displayName;
            this.colorCode = colorCode;
            this.icon = icon;
            this.durationMin = durationMin;
            this.happinessDelta = happinessDelta;
            this.appealDelta = appealDelta;
            this.foodDelta = foodDelta;
            this.incomeCode = incomeCode;
            this.oneTimeTheft = oneTimeTheft;
            this.playerBuff = playerBuff;
            this.flavour = flavour;
        }

        public String colored() {
            return colorCode + displayName;
        }

        public boolean isPositive() {
            return happinessDelta + appealDelta + foodDelta >= 0 && !oneTimeTheft;
        }

        public long incomePerCycle() {
            return incomeCode > 0 ? Math.min(MAX_INCOME_PER_CYCLE, incomeCode) : 0L;
        }

        public static Type byName(String n) {
            if (n == null) return null;
            for (Type t : values()) if (t.name().equalsIgnoreCase(n)) return t;
            return null;
        }
    }

    // ── Persisted state ──────────────────────────────────────────────────────

    public static class ActiveEvent {
        public String settlementId;
        public String type = Type.FESTIVAL.name();
        public long startedAtMs;
        public long endsAtMs;

        public Type typeEnum() {
            Type t = Type.byName(type);
            return t == null ? Type.FESTIVAL : t;
        }
    }

    /** settlementId -> its single active event. */
    private static final Map<String, ActiveEvent> active = new LinkedHashMap<>();
    /** "settlementId:field" -> the exact value we last wrote (apply-once guard). Transient. */
    private static final Map<String, Integer> lastWritten = new HashMap<>();

    private static Path dataFile;
    private static long lastRotateMs = 0L;
    private static int applyCounter = 0;
    private static int incomeCounter = 0;
    private static final Random RNG = new Random();

    private static class SaveFile {
        List<ActiveEvent> events = new ArrayList<>();
        long lastRotateMs = 0L;
    }

    // ── Persistence ──────────────────────────────────────────────────────────

    public static void initialize(Path worldRoot) {
        dataFile = worldRoot.resolve("settlement_events.json");
        active.clear();
        lastWritten.clear();
        if (dataFile == null || !Files.exists(dataFile)) {
            lastRotateMs = System.currentTimeMillis();
            return;
        }
        try (Reader r = Files.newBufferedReader(dataFile)) {
            Gson g = new GsonBuilder().create();
            java.lang.reflect.Type t = new TypeToken<SaveFile>() {}.getType();
            SaveFile sf = g.fromJson(r, t);
            if (sf != null && sf.events != null) {
                for (ActiveEvent e : sf.events) {
                    if (e != null && e.settlementId != null) active.put(e.settlementId, e);
                }
                lastRotateMs = sf.lastRotateMs;
            }
        } catch (Exception e) {
            System.err.println("[SettlementEvents] Failed to load: " + e.getMessage());
        }
        if (lastRotateMs == 0L) lastRotateMs = System.currentTimeMillis();
    }

    public static void save() {
        if (dataFile == null) return;
        try {
            Files.createDirectories(dataFile.getParent());
            SaveFile sf = new SaveFile();
            sf.events = new ArrayList<>(active.values());
            sf.lastRotateMs = lastRotateMs;
            Gson g = new GsonBuilder().setPrettyPrinting().create();
            try (Writer w = Files.newBufferedWriter(dataFile)) {
                g.toJson(sf, w);
            }
        } catch (Exception e) {
            System.err.println("[SettlementEvents] Failed to save: " + e.getMessage());
        }
    }

    // ── Queries (for the GUIs) ───────────────────────────────────────────────

    public static ActiveEvent activeFor(String settlementId) {
        if (settlementId == null) return null;
        ActiveEvent e = active.get(settlementId);
        if (e == null) return null;
        return e.endsAtMs > System.currentTimeMillis() ? e : null;
    }

    public static List<ActiveEvent> allActive() {
        long now = System.currentTimeMillis();
        List<ActiveEvent> out = new ArrayList<>();
        for (ActiveEvent e : active.values()) {
            if (e.endsAtMs > now) out.add(e);
        }
        return out;
    }

    public static long remainingMs(ActiveEvent e) {
        return e == null ? 0L : Math.max(0L, e.endsAtMs - System.currentTimeMillis());
    }

    // ── Tick ─────────────────────────────────────────────────────────────────

    public static void tick(MinecraftServer server) {
        if (server == null) return;
        long now = System.currentTimeMillis();

        // 1) Expiry sweep + reapply of live deltas.
        if (++applyCounter >= APPLY_INTERVAL_TICKS) {
            applyCounter = 0;
            expireSweep(server, now);
            reapplyAll();
            buffPlayersInEvents(server);
        }

        // 2) Income banking on the economy cycle.
        if (++incomeCounter >= INCOME_CYCLE_TICKS) {
            incomeCounter = 0;
            bankIncome();
        }

        // 3) Rotation: maybe start a new event.
        if (now - lastRotateMs >= ROTATE_INTERVAL_MS) {
            lastRotateMs = now;
            maybeStartEvent(server, now);
            save();
        }
    }

    // ── Rotation / lifecycle ─────────────────────────────────────────────────

    private static void maybeStartEvent(MinecraftServer server, long now) {
        if (allActive().size() >= MAX_CONCURRENT) return;
        if (RNG.nextDouble() > FIRE_CHANCE) return;

        // Eligible: has citizens and no live event. Empty outposts get no events.
        List<Settlement> eligible = new ArrayList<>();
        for (Settlement s : SettlementManager.getAllSettlements()) {
            if (s.population <= 0) continue;
            if (activeFor(s.id) != null) continue;
            eligible.add(s);
        }
        if (eligible.isEmpty()) return;

        Settlement target = eligible.get(RNG.nextInt(eligible.size()));
        Type[] all = Type.values();
        Type type = all[RNG.nextInt(all.length)];
        start(server, target, type, now);
    }

    /** Begin an event on a settlement (also the operator/GUI entry point). */
    public static void start(MinecraftServer server, Settlement s, Type type, long now) {
        if (s == null || type == null) return;
        ActiveEvent e = new ActiveEvent();
        e.settlementId = s.id;
        e.type = type.name();
        e.startedAtMs = now;
        e.endsAtMs = now + type.durationMin * 60_000L;
        active.put(s.id, e);

        // One-time treasury shock at start.
        if (type.oneTimeTheft) {
            long amt = Math.min(ONE_TIME_ABS_CAP, Math.round(s.treasury * ONE_TIME_FRACTION));
            if (amt > 0) {
                s.treasury = Math.max(0L, s.treasury - amt);
                SettlementManager.save();
            }
        } else if (type == Type.GOLD_RUSH) {
            // A modest capped windfall to kick off a gold rush.
            long amt = Math.min(ONE_TIME_ABS_CAP, 1_500L);
            s.treasury += amt;
            SettlementManager.save();
        }

        announce(server, s, Component.literal(type.colorCode + "✦ " + type.displayName + " in " + s.name + "!")
                .withStyle(type.isPositive() ? ChatFormatting.GREEN : ChatFormatting.RED));
        announce(server, s, Component.literal("§7" + type.flavour));
        announce(server, s, Component.literal("§8Lasts " + type.durationMin + " min. §7See §f/events§7."));
        playAtSettlement(server, s, type.isPositive() ? 1.3f : 0.7f);
        save();
    }

    /** End an event early (operator/GUI). */
    public static void clear(MinecraftServer server, String settlementId) {
        ActiveEvent e = active.remove(settlementId);
        if (e == null) return;
        forgetWrites(settlementId);
        Settlement s = SettlementManager.get(settlementId);
        if (s != null && server != null) {
            announce(server, s, Component.literal("§7The " + e.typeEnum().displayName
                    + " in " + s.name + " has passed."));
        }
        save();
    }

    private static void expireSweep(MinecraftServer server, long now) {
        List<String> expired = new ArrayList<>();
        for (ActiveEvent e : active.values()) {
            if (e.endsAtMs <= now) expired.add(e.settlementId);
        }
        for (String id : expired) {
            ActiveEvent e = active.remove(id);
            forgetWrites(id);
            Settlement s = SettlementManager.get(id);
            if (s != null && e != null) {
                announce(server, s, Component.literal("§7The " + e.typeEnum().displayName
                        + " in " + s.name + " has run its course."));
            }
        }
        if (!expired.isEmpty()) save();
    }

    // ── Effect application (apply-once-until-changed) ────────────────────────

    private static void reapplyAll() {
        for (ActiveEvent e : allActive()) {
            Settlement s = SettlementManager.get(e.settlementId);
            if (s == null || s.population <= 0) continue;
            Type t = e.typeEnum();
            if (t.happinessDelta != 0) {
                s.happiness = applyOnce(e.settlementId, "hap", s.happiness, t.happinessDelta, 0, 100);
            }
            if (t.appealDelta != 0) {
                s.appeal = applyOnce(e.settlementId, "app", s.appeal, t.appealDelta, 0, Integer.MAX_VALUE);
            }
            if (t.foodDelta != 0) {
                s.food = applyOnce(e.settlementId, "food", s.food, t.foodDelta, 0, Integer.MAX_VALUE);
            }
        }
    }

    /**
     * The heart of the no-edit design. If the field still holds the exact value we last wrote, do
     * nothing (idempotent). Otherwise something reset it to a fresh base — apply the delta once and
     * record the result. Prevents any runaway stacking.
     */
    private static int applyOnce(String id, String field, int current, int delta, int lo, int hi) {
        String key = id + ":" + field;
        Integer wrote = lastWritten.get(key);
        if (wrote != null && wrote == current) {
            return current; // our value still stands
        }
        int applied = current + delta;
        if (applied < lo) applied = lo;
        if (applied > hi) applied = hi;
        lastWritten.put(key, applied);
        return applied;
    }

    private static void forgetWrites(String settlementId) {
        lastWritten.keySet().removeIf(k -> k.startsWith(settlementId + ":"));
    }

    private static void bankIncome() {
        boolean dirty = false;
        for (ActiveEvent e : allActive()) {
            Settlement s = SettlementManager.get(e.settlementId);
            if (s == null || s.population <= 0) continue;
            long inc = e.typeEnum().incomePerCycle();
            if (inc > 0) {
                s.treasury += inc;
                dirty = true;
            }
        }
        if (dirty) SettlementManager.save();
    }

    /** Weak, short, hidden comfort/misery cue for players standing in an event settlement. */
    private static void buffPlayersInEvents(MinecraftServer server) {
        for (ActiveEvent e : allActive()) {
            Type t = e.typeEnum();
            if (!t.playerBuff) continue;
            Settlement s = SettlementManager.get(e.settlementId);
            if (s == null) continue;
            ServerLevel world = SettlementManager.levelFor(server, s);
            if (world == null) continue;
            for (ServerPlayer p : world.players()) {
                if (p.distanceToSqr(s.cornerstoneX, s.cornerstoneY, s.cornerstoneZ) < 200 * 200) {
                    // 3s, no icon, no stack of value — cosmetic celebration cue only.
                    p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false, false));
                }
            }
        }
    }

    // ── Announce helpers ─────────────────────────────────────────────────────

    private static void announce(MinecraftServer server, Settlement s, Component msg) {
        if (server == null || s == null) return;
        ServerLevel world = SettlementManager.levelFor(server, s);
        if (world == null) return;
        for (ServerPlayer p : world.players()) {
            if (p.distanceToSqr(s.cornerstoneX, s.cornerstoneY, s.cornerstoneZ) < 300 * 300) {
                p.sendSystemMessage(msg);
            }
        }
    }

    private static void playAtSettlement(MinecraftServer server, Settlement s, float pitch) {
        ServerLevel world = SettlementManager.levelFor(server, s);
        if (world == null) return;
        world.playSound(null, s.cornerstonePos(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 0.7f, pitch);
    }
}
