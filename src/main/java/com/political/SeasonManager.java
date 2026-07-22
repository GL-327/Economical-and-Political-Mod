package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The seasonal clock of the Living World.
 *
 * <h2>How time is measured</h2>
 * Seasons advance on <b>real wall-clock time</b>, not tick count, so a season keeps running at
 * the same rate whether the server is lagging, whether players are online, and across restarts.
 * The default length is one Minecraft fortnight of real time: 14 MC days × 20 real minutes =
 * 280 minutes ≈ 4h40m per season, so a full year is a little under 19 real hours.
 *
 * <h2>What a season touches</h2>
 * <ul>
 *   <li>{@link #cropGrowthMultiplier()} — read by {@code SeasonCropGrowthMixin} on
 *       {@code CropBlock.randomTick}. Above 1.0 grants extra growth rolls, below 1.0 cancels
 *       a proportion of them.</li>
 *   <li>{@link #foodMultiplier()} — intended to be multiplied into the {@code food} figure in
 *       {@code SettlementEconomy.runCycle}. Exposed rather than injected so the economy owner
 *       decides exactly where it lands.</li>
 *   <li>{@link #applySeasonalFlavour(LivingEntity)} — small, purely flavourful modifiers on
 *       newly loaded hostile mobs.</li>
 *   <li>{@link #tickAmbient(MinecraftServer)} — a brief seasonal effect for players under
 *       open sky.</li>
 * </ul>
 *
 * <h2>Why this cannot be farmed</h2>
 * Nothing here is triggered by a player action. The clock runs on wall time, ambient effects
 * are short, weak, and rate-limited per player, and the food multiplier applies to settlement
 * production that is already gated by beds, workstations and happiness. There is no input a
 * player can spam to make a season pay out more.
 */
public final class SeasonManager {

    private SeasonManager() {}

    // ── Tunables ────────────────────────────────────────────────────────────

    /** Default season length: 14 Minecraft days of real time (14 × 20 min). */
    public static final long DEFAULT_SEASON_LENGTH_MS = 280L * 60L * 1000L;

    /** Hard floor so an operator cannot set a length that spams the broadcast. */
    private static final long MIN_SEASON_LENGTH_MS = 5L * 60L * 1000L;

    /** How often the clock is reconciled against wall time. */
    private static final int CLOCK_INTERVAL_TICKS = 40;

    /** How often ambient seasonal effects are considered, per player. */
    private static final int AMBIENT_INTERVAL_TICKS = 400;

    // ── State ───────────────────────────────────────────────────────────────

    private static class SaveFile {
        int seasonOrdinal = 0;
        long elapsedMs = 0L;
        long seasonLengthMs = DEFAULT_SEASON_LENGTH_MS;
        long yearsElapsed = 0L;
        boolean enabled = true;
    }

    private static Season current = Season.SPRING;
    private static long elapsedMs = 0L;
    private static long seasonLengthMs = DEFAULT_SEASON_LENGTH_MS;
    private static long yearsElapsed = 0L;
    private static boolean enabled = true;

    /** Wall-clock stamp of the last reconcile; not persisted (restart resumes from elapsedMs). */
    private static long lastRealMs = 0L;

    private static int tickCounter = 0;
    private static int ambientCounter = 0;

    private static Path file;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ── Persistence ─────────────────────────────────────────────────────────

    public static void initialize(Path worldRoot) {
        file = worldRoot.resolve("seasons.json");
        if (Files.exists(file)) {
            try (Reader r = Files.newBufferedReader(file)) {
                SaveFile sf = GSON.fromJson(r, new TypeToken<SaveFile>() {}.getType());
                if (sf != null) {
                    Season[] all = Season.values();
                    int ord = Math.max(0, Math.min(all.length - 1, sf.seasonOrdinal));
                    current = all[ord];
                    elapsedMs = Math.max(0L, sf.elapsedMs);
                    seasonLengthMs = Math.max(MIN_SEASON_LENGTH_MS, sf.seasonLengthMs);
                    yearsElapsed = Math.max(0L, sf.yearsElapsed);
                    enabled = sf.enabled;
                }
            } catch (Exception e) {
                PoliticalServer.LOGGER.warn("Failed to load seasons.json", e);
            }
        }
        lastRealMs = System.currentTimeMillis();
    }

    public static void save() {
        if (file == null) return;
        try (Writer w = Files.newBufferedWriter(file)) {
            SaveFile sf = new SaveFile();
            sf.seasonOrdinal = current.ordinal();
            sf.elapsedMs = elapsedMs;
            sf.seasonLengthMs = seasonLengthMs;
            sf.yearsElapsed = yearsElapsed;
            sf.enabled = enabled;
            GSON.toJson(sf, w);
        } catch (Exception e) {
            PoliticalServer.LOGGER.warn("Failed to save seasons.json", e);
        }
    }

    // ── Queries ─────────────────────────────────────────────────────────────

    public static Season current() {
        return current;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static long seasonLengthMs() {
        return seasonLengthMs;
    }

    public static long elapsedMs() {
        return elapsedMs;
    }

    public static long remainingMs() {
        return Math.max(0L, seasonLengthMs - elapsedMs);
    }

    public static long yearsElapsed() {
        return yearsElapsed;
    }

    /** 0.0 .. 1.0 through the current season. */
    public static double progress() {
        if (seasonLengthMs <= 0) return 0.0;
        return Math.max(0.0, Math.min(1.0, (double) elapsedMs / (double) seasonLengthMs));
    }

    /** Crop random-tick growth multiplier. 1.0 when seasons are disabled. */
    public static double cropGrowthMultiplier() {
        if (!enabled) return 1.0;
        return current.cropGrowth;
    }

    /**
     * Multiplier that {@code SettlementEconomy} should apply to its computed FOOD figure.
     * Returns 1.0 when seasons are disabled so the call site is always safe.
     */
    public static double foodMultiplier() {
        if (!enabled) return 1.0;
        return current.foodMultiplier;
    }

    /** Convenience for the economy: apply the seasonal food multiplier to a raw food value. */
    public static double applyFood(double rawFood) {
        return rawFood * foodMultiplier();
    }

    // ── Admin ───────────────────────────────────────────────────────────────

    public static void setEnabled(boolean value) {
        enabled = value;
        save();
    }

    public static void setSeasonLengthMs(long ms) {
        seasonLengthMs = Math.max(MIN_SEASON_LENGTH_MS, ms);
        if (elapsedMs > seasonLengthMs) elapsedMs = seasonLengthMs - 1;
        save();
    }

    /** Force the season. Resets the elapsed clock and broadcasts as a normal change. */
    public static void setSeason(MinecraftServer server, Season season, boolean broadcast) {
        if (season == null) return;
        current = season;
        elapsedMs = 0L;
        lastRealMs = System.currentTimeMillis();
        save();
        if (broadcast) broadcastChange(server);
    }

    /** Skip straight to the next season. */
    public static void advance(MinecraftServer server) {
        setSeason(server, current.next(), true);
    }

    // ── Tick ────────────────────────────────────────────────────────────────

    public static void tick(MinecraftServer server) {
        if (server == null) return;

        if (++tickCounter >= CLOCK_INTERVAL_TICKS) {
            tickCounter = 0;
            advanceClock(server);
        }

        if (++ambientCounter >= AMBIENT_INTERVAL_TICKS) {
            ambientCounter = 0;
            tickAmbient(server);
        }
    }

    private static void advanceClock(MinecraftServer server) {
        long now = System.currentTimeMillis();
        if (lastRealMs == 0L) {
            lastRealMs = now;
            return;
        }
        long delta = now - lastRealMs;
        lastRealMs = now;

        // Guard against clock jumps (sleep/resume, NTP correction, long freeze).
        if (delta <= 0) return;
        if (delta > 60L * 60L * 1000L) delta = 60L * 60L * 1000L;

        if (!enabled) return;

        elapsedMs += delta;

        boolean changed = false;
        // Loop in case a very long delta spans more than one season.
        while (elapsedMs >= seasonLengthMs) {
            elapsedMs -= seasonLengthMs;
            Season previous = current;
            current = current.next();
            if (previous == Season.WINTER) yearsElapsed++;
            changed = true;
        }

        if (changed) {
            save();
            broadcastChange(server);
        }
    }

    private static void broadcastChange(MinecraftServer server) {
        if (server == null) return;
        Season s = current;

        Component header = Component.literal("═══════════════════════════════════")
                .withStyle(ChatFormatting.DARK_GRAY);
        Component title = Component.literal(s.symbol + " " + s.displayName.toUpperCase() + " HAS COME "
                + s.symbol).withStyle(s.color, ChatFormatting.BOLD);

        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(header);
            p.sendSystemMessage(title);
            p.sendSystemMessage(Component.literal("§7" + s.descriptionLine1));
            p.sendSystemMessage(Component.literal("§7" + s.descriptionLine2));
            p.sendSystemMessage(Component.literal("  " + s.effectSummary()));
            p.sendSystemMessage(Component.literal("§8Open §7/season§8 for the full almanac."));
            p.sendSystemMessage(header);

            if (p.level() instanceof ServerLevel sl) {
                sl.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                        SoundSource.PLAYERS, 0.7f, seasonPitch(s));
            }
        }
        PoliticalServer.LOGGER.info("[Seasons] Season changed to {} (year {})", s.displayName, yearsElapsed + 1);
    }

    private static float seasonPitch(Season s) {
        return switch (s) {
            case SPRING -> 1.4f;
            case SUMMER -> 1.2f;
            case AUTUMN -> 1.0f;
            case WINTER -> 0.7f;
        };
    }

    // ── Ambient effect ──────────────────────────────────────────────────────

    /**
     * A short, weak seasonal effect for players standing under open sky. Effects are
     * intentionally cosmetic-adjacent: they last a handful of seconds, never stack, and give
     * nothing an AFK player could bank (no saturation, no regeneration of resources, no coins).
     */
    public static void tickAmbient(MinecraftServer server) {
        if (server == null || !enabled) return;
        Season s = current;

        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (!(p.level() instanceof ServerLevel level)) continue;
            if (p.isSpectator() || p.getAbilities().instabuild) continue;

            BlockPos pos = p.blockPosition();
            if (!level.canSeeSky(pos)) continue;

            java.util.Random rng = new java.util.Random(
                    p.getUUID().hashCode() ^ (level.getGameTime() / AMBIENT_INTERVAL_TICKS));

            switch (s) {
                case SPRING -> {
                    if (rng.nextFloat() < 0.30f) {
                        p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, true, false, false));
                    }
                }
                case SUMMER -> {
                    if (isDaytime(level) && rng.nextFloat() < 0.30f) {
                        p.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 0, true, false, false));
                    }
                }
                case AUTUMN -> {
                    if (rng.nextFloat() < 0.30f) {
                        p.addEffect(new MobEffectInstance(MobEffects.LUCK, 300, 0, true, false, false));
                    }
                }
                case WINTER -> {
                    // Winter bites at night, and only outdoors.
                    if (!isDaytime(level) && rng.nextFloat() < 0.35f) {
                        p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 0, true, false, false));
                    }
                }
            }
        }
    }

    private static boolean isDaytime(ServerLevel level) {
        // 26.1: ServerLevel exposes isBrightOutside() rather than getDayTime().
        return level.isBrightOutside();
    }

    // ── Mob flavour ─────────────────────────────────────────────────────────

    /**
     * Seasonal flavour on newly loaded hostile mobs. Deliberately tiny — one hidden effect on
     * roughly one mob in six — so seasons colour combat without turning winter into a wall.
     */
    public static void applySeasonalFlavour(LivingEntity entity) {
        if (!enabled) return;
        if (entity == null || !entity.isAlive()) return;
        if (!(entity instanceof Mob mob)) return;
        if (!(entity instanceof Monster)) return;
        if (mob.entityTags().contains(HuntManager.HUNT_TAG)) return;   // hunts are already dressed
        if (mob.entityTags().contains(SEASON_FLAVOUR_TAG)) return;

        if (mob.level().getRandom().nextFloat() >= 0.17f) return;
        mob.addTag(SEASON_FLAVOUR_TAG);

        switch (current) {
            case SPRING -> mob.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 999999, 0, true, false, false));
            case SUMMER -> mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 999999, 0, true, false, false));
            case AUTUMN -> mob.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 999999, 0, true, false, false));
            case WINTER -> mob.addEffect(new MobEffectInstance(MobEffects.SPEED, 999999, 0, true, false, false));
        }
    }

    public static final String SEASON_FLAVOUR_TAG = "political_season_flavour";

    // ── Formatting helpers used by the GUI ──────────────────────────────────

    public static String formatRemaining() {
        return PoliticalServer.formatTime(remainingMs());
    }

    /** A 20-cell progress bar for the current season. */
    public static String progressBar() {
        int filled = (int) Math.round(progress() * 20);
        StringBuilder sb = new StringBuilder(current.legacy);
        for (int i = 0; i < 20; i++) {
            if (i == filled) sb.append("§8");
            sb.append('|');
        }
        return sb.toString();
    }
}
