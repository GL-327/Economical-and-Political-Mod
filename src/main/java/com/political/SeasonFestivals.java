package com.political;

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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * V6 variety — a festival that rides on top of each {@link Season}.
 *
 * <p>{@link SeasonManager} is read, never edited. Every season has one signature festival that runs
 * during a fixed window of that season (the back third, when the season "peaks"). While a festival
 * is live it adds a distinct mob/loot flavour on top of whatever {@link SeasonManager} already does,
 * and a weak, short, hidden lift for players under open sky. Festival state is derived from the
 * season clock, so it needs almost no storage — only an edge marker so start/end are announced once.</p>
 *
 * <h2>Wiring (no edit by me)</h2>
 * <ul>
 *   <li>Tick: call {@link #tick(MinecraftServer)} once per server tick (next to
 *       {@code SeasonManager.tick}).</li>
 *   <li>Mob flavour: call {@link #applyFestivalFlavour(LivingEntity)} from the same entity-load /
 *       spawn hook that already calls {@code SeasonManager.applySeasonalFlavour(entity)} (an
 *       eventHook — I do not edit that file).</li>
 *   <li>Persistence: {@link #initialize(Path)} / {@link #save()} alongside the other managers.</li>
 * </ul>
 *
 * <h2>Balance / anti-farm</h2>
 * Festivals are driven purely by the wall-clock season, never by any player action. The mob flavour
 * is one hidden effect on a small fraction of mobs (never a difficulty wall), the player lift is a
 * 5-second no-icon effect, and the "loot flavour" is a single extra common drop at a capped low
 * chance — it can never out-earn combat the player was already doing.
 */
public final class SeasonFestivals {

    private SeasonFestivals() {}

    // Festival window inside a season, as a fraction of the season length.
    private static final double WINDOW_START = 0.60;
    private static final double WINDOW_END   = 0.92;

    /** One festival per season. */
    public enum Festival {
        BLOOMFEST   (Season.SPRING, "Bloomfest",    "§a", Items.PINK_TULIP,
                "Garlands and green boughs — the world is reborn.", MobEffects.JUMP_BOOST, Items.MELON_SLICE),
        SUNHIGH_FAIR(Season.SUMMER, "Sunhigh Fair", "§e", Items.SUNFLOWER,
                "Games, ale and long bright evenings.", MobEffects.FIRE_RESISTANCE, Items.GOLDEN_CARROT),
        HARVEST_HOME(Season.AUTUMN, "Harvest Home", "§6", Items.HAY_BLOCK,
                "The barns are full and the tables groan.", MobEffects.RESISTANCE, Items.BREAD),
        LONGNIGHT   (Season.WINTER, "Longnight",    "§b", Items.SOUL_LANTERN,
                "Lanterns against the dark and the cold.", MobEffects.SPEED, Items.COOKED_BEEF);

        public final Season season;
        public final String displayName;
        public final String colorCode;
        public final Item icon;
        public final String flavour;
        public final net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> mobEffect;
        public final Item loot;

        Festival(Season season, String displayName, String colorCode, Item icon, String flavour,
                 net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> mobEffect, Item loot) {
            this.season = season;
            this.displayName = displayName;
            this.colorCode = colorCode;
            this.icon = icon;
            this.flavour = flavour;
            this.mobEffect = mobEffect;
            this.loot = loot;
        }

        public String colored() {
            return colorCode + displayName;
        }

        public static Festival forSeason(Season s) {
            for (Festival f : values()) if (f.season == s) return f;
            return BLOOMFEST;
        }
    }

    public static final String FESTIVAL_FLAVOUR_TAG = "political_festival_flavour";

    // ── State (edge marker only) ─────────────────────────────────────────────

    private static Path file;
    private static boolean lastActive = false;
    private static int lastSeasonOrdinal = -1;
    private static long lastYear = -1;
    private static int ambientCounter = 0;

    private static class SaveFile {
        boolean lastActive = false;
        int lastSeasonOrdinal = -1;
        long lastYear = -1;
    }

    public static void initialize(Path worldRoot) {
        file = worldRoot.resolve("season_festivals.json");
        if (file != null && Files.exists(file)) {
            try (Reader r = Files.newBufferedReader(file)) {
                SaveFile sf = new GsonBuilder().create().fromJson(r, new TypeToken<SaveFile>() {}.getType());
                if (sf != null) {
                    lastActive = sf.lastActive;
                    lastSeasonOrdinal = sf.lastSeasonOrdinal;
                    lastYear = sf.lastYear;
                }
            } catch (Exception e) {
                System.err.println("[SeasonFestivals] Failed to load: " + e.getMessage());
            }
        }
    }

    public static void save() {
        if (file == null) return;
        try {
            SaveFile sf = new SaveFile();
            sf.lastActive = lastActive;
            sf.lastSeasonOrdinal = lastSeasonOrdinal;
            sf.lastYear = lastYear;
            Gson g = new GsonBuilder().setPrettyPrinting().create();
            try (Writer w = Files.newBufferedWriter(file)) {
                g.toJson(sf, w);
            }
        } catch (Exception e) {
            System.err.println("[SeasonFestivals] Failed to save: " + e.getMessage());
        }
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    public static boolean isActive() {
        if (!SeasonManager.isEnabled()) return false;
        double p = SeasonManager.progress();
        return p >= WINDOW_START && p < WINDOW_END;
    }

    public static Festival current() {
        return Festival.forSeason(SeasonManager.current());
    }

    /** Ms until the festival ends (if active) or begins (if not yet). 0 if past this season's window. */
    public static long timeUntilEdgeMs() {
        double p = SeasonManager.progress();
        long len = SeasonManager.seasonLengthMs();
        if (p < WINDOW_START) return Math.round((WINDOW_START - p) * len);
        if (p < WINDOW_END) return Math.round((WINDOW_END - p) * len);
        return 0L;
    }

    public static String statusLine() {
        Festival f = current();
        if (isActive()) {
            return f.colored() + " §7is underway — ends in §f" + PoliticalServer.formatTime(timeUntilEdgeMs());
        }
        double p = SeasonManager.progress();
        if (p < WINDOW_START) {
            return f.colored() + " §7begins in §f" + PoliticalServer.formatTime(timeUntilEdgeMs());
        }
        return f.colored() + " §7has passed for this " + SeasonManager.current().displayName;
    }

    // ── Tick ─────────────────────────────────────────────────────────────────

    public static void tick(MinecraftServer server) {
        if (server == null) return;

        boolean nowActive = isActive();
        int ord = SeasonManager.current().ordinal();
        long year = SeasonManager.yearsElapsed();

        // A festival "instance" is identified by (season, year). Announce start/end on edges.
        if (nowActive && (!lastActive || ord != lastSeasonOrdinal || year != lastYear)) {
            announceStart(server);
        } else if (!nowActive && lastActive) {
            announceEnd(server);
        }
        if (nowActive != lastActive || ord != lastSeasonOrdinal || year != lastYear) {
            lastActive = nowActive;
            lastSeasonOrdinal = ord;
            lastYear = year;
            save();
        }

        // Ambient lift for players under open sky during a festival.
        if (nowActive && ++ambientCounter >= 200) {
            ambientCounter = 0;
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                if (!(p.level() instanceof ServerLevel level)) continue;
                if (p.isSpectator() || p.getAbilities().instabuild) continue;
                if (!level.canSeeSky(p.blockPosition())) continue;
                p.addEffect(new MobEffectInstance(MobEffects.LUCK, 120, 0, true, false, false));
            }
        }
    }

    private static void announceStart(MinecraftServer server) {
        Festival f = current();
        Component bar = Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.DARK_GRAY);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(bar);
            p.sendSystemMessage(Component.literal(f.colorCode + "✦ " + f.displayName.toUpperCase() + " BEGINS ✦")
                    .withStyle(ChatFormatting.BOLD));
            p.sendSystemMessage(Component.literal("§7" + f.flavour));
            p.sendSystemMessage(Component.literal("§8Seasonal festival — open §7/world§8 to see it."));
            p.sendSystemMessage(bar);
            if (p.level() instanceof ServerLevel sl) {
                sl.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                        SoundSource.PLAYERS, 0.7f, 1.5f);
            }
        }
        PoliticalServer.LOGGER.info("[SeasonFestivals] {} begins.", f.displayName);
    }

    private static void announceEnd(MinecraftServer server) {
        Festival f = current();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.literal("§7The " + f.displayName + " has ended for this year."));
        }
    }

    // ── Mob / loot flavour ───────────────────────────────────────────────────

    /**
     * Adds a festival cue to a small fraction of hostile mobs while a festival is live. Additive to
     * {@code SeasonManager.applySeasonalFlavour} — uses its own tag so the two never collide.
     */
    public static void applyFestivalFlavour(LivingEntity entity) {
        if (!isActive()) return;
        if (entity == null || !entity.isAlive()) return;
        if (!(entity instanceof Mob mob) || !(entity instanceof Monster)) return;
        if (mob.entityTags().contains(FESTIVAL_FLAVOUR_TAG)) return;
        if (mob.level().getRandom().nextFloat() >= 0.12f) return;
        mob.addTag(FESTIVAL_FLAVOUR_TAG);
        mob.addEffect(new MobEffectInstance(current().mobEffect, 999999, 0, true, false, false));
        // A festive name so players notice the flavour.
        mob.setCustomName(Component.literal(current().colorCode + "✦ ").append(
                mob.getName()));
    }

    /**
     * One optional extra common drop for a festival kill, at a capped low chance. Returns the item
     * to drop or {@code ItemStack.EMPTY}. Exposed for the loot/death hook to use if desired.
     */
    public static net.minecraft.world.item.ItemStack festivalLootRoll(java.util.Random rng) {
        if (!isActive() || rng == null) return net.minecraft.world.item.ItemStack.EMPTY;
        if (rng.nextFloat() >= 0.08f) return net.minecraft.world.item.ItemStack.EMPTY;
        return new net.minecraft.world.item.ItemStack(current().loot, 1);
    }
}
