package com.political;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Random;

/**
 * V6 variety — world-wide rotating modifiers. At most ONE is ever active. A modifier tweaks drop
 * rates / mob flavour / trade for a capped window, is announced to everyone, and is inspectable in
 * {@link VarietyGui}.
 *
 * <h2>Wiring (no edit by me)</h2>
 * <ul>
 *   <li>Tick: call {@link #tick(MinecraftServer)} once per server tick.</li>
 *   <li>Persistence: {@link #initialize(Path)} / {@link #save()}.</li>
 *   <li>Mob flavour: call {@link #applyModifierFlavour(LivingEntity)} from the same entity-load hook
 *       that feeds {@code SeasonManager.applySeasonalFlavour}.</li>
 *   <li>Loot / coin hooks are pull-based: any code that wants to honour a modifier reads
 *       {@link #extraDropChance()} or {@link #coinBonusFactor()} — nothing is pushed into existing
 *       systems, so wiring is optional and additive.</li>
 * </ul>
 *
 * <h2>Balance / anti-farm</h2>
 * Modifiers start only on this class's own timer, never by a player. Exactly one runs at a time,
 * each lasts a capped window and is followed by a cooldown, magnitudes are hard-capped constants
 * ({@link #extraDropChance()} ≤ 0.12, {@link #coinBonusFactor()} ≤ 1.15) and every effect is
 * temporary — nothing here is permanent, stackable, or reachable by grinding.
 */
public final class WorldModifiers {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldModifiers.class);


    private WorldModifiers() {}

    // ── Tunables ─────────────────────────────────────────────────────────────
    private static final long MIN_COOLDOWN_MS = 30L * 60L * 1000L;  // ≥30 min quiet between modifiers
    private static final long MAX_COOLDOWN_MS = 55L * 60L * 1000L;
    private static final double MAX_DROP_BONUS = 0.12;
    private static final double MAX_COIN_FACTOR = 1.15;

    /** A world-wide modifier. All effect magnitudes are already clamped at declaration. */
    public enum Modifier {
        CURSED_MOON     ("Cursed Moon",         "§5", Items.CRYING_OBSIDIAN, 18,
                "The moon runs red-black. Night things move quicker.",
                0.00, 1.00, MobEffects.SPEED),
        BLOOD_MOON      ("Blood Moon",          "§c", Items.REDSTONE_BLOCK, 15,
                "A hunter's moon. Monsters come emboldened — and richer.",
                0.05, 1.00, MobEffects.STRENGTH),
        MERCHANT_WEEK   ("Merchant Caravan Week","§6", Items.EMERALD_BLOCK, 25,
                "The roads throng with traders; purses come easier.",
                0.00, 1.10, null),
        BOUNTIFUL_EARTH ("Bountiful Earth",     "§a", Items.HAY_BLOCK, 22,
                "The land is generous; the world gives up a little more.",
                0.12, 1.00, null),
        GILDED_HOUR     ("Gilded Hour",         "§e", Items.GOLD_BLOCK, 12,
                "A brief golden spell — fortune smiles on commerce.",
                0.03, 1.15, null),
        WANDERING_SPIRITS("Wandering Spirits",  "§b", Items.SOUL_LANTERN, 16,
                "Pale lights drift the dark. The slain leave keepsakes.",
                0.04, 1.00, MobEffects.GLOWING),
        FAIR_WINDS      ("Fair Winds",          "§f", Items.FEATHER, 20,
                "Clear skies and a following wind across the world.",
                0.02, 1.00, null);

        public final String displayName;
        public final String colorCode;
        public final Item icon;
        public final int durationMin;
        public final String flavour;
        public final double dropBonus;
        public final double coinFactor;
        public final net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> mobEffect;

        Modifier(String displayName, String colorCode, Item icon, int durationMin, String flavour,
                 double dropBonus, double coinFactor,
                 net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> mobEffect) {
            this.displayName = displayName;
            this.colorCode = colorCode;
            this.icon = icon;
            this.durationMin = durationMin;
            this.flavour = flavour;
            this.dropBonus = dropBonus;
            this.coinFactor = coinFactor;
            this.mobEffect = mobEffect;
        }

        public String colored() {
            return colorCode + displayName;
        }

        public static Modifier byName(String n) {
            if (n == null) return null;
            for (Modifier m : values()) if (m.name().equalsIgnoreCase(n)) return m;
            return null;
        }
    }

    public static final String MODIFIER_FLAVOUR_TAG = "political_worldmod_flavour";

    // ── State ────────────────────────────────────────────────────────────────
    private static Path file;
    private static String activeName = null;
    private static long endsAtMs = 0L;
    private static long nextEligibleMs = 0L;
    private static final Random RNG = new Random();

    private static class SaveFile {
        String activeName;
        long endsAtMs;
        long nextEligibleMs;
    }

    public static void initialize(Path worldRoot) {
        file = worldRoot.resolve("world_modifiers.json");
        activeName = null;
        endsAtMs = 0L;
        nextEligibleMs = 0L;
        if (file != null && Files.exists(file)) {
            try (Reader r = Files.newBufferedReader(file)) {
                SaveFile sf = new GsonBuilder().create().fromJson(r, new TypeToken<SaveFile>() {}.getType());
                if (sf != null) {
                    activeName = sf.activeName;
                    endsAtMs = sf.endsAtMs;
                    nextEligibleMs = sf.nextEligibleMs;
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load", e);
            }
        }
        long now = System.currentTimeMillis();
        if (nextEligibleMs == 0L) nextEligibleMs = now + MIN_COOLDOWN_MS;
    }

    public static void save() {
        if (file == null) return;
        try {
            SaveFile sf = new SaveFile();
            sf.activeName = activeName;
            sf.endsAtMs = endsAtMs;
            sf.nextEligibleMs = nextEligibleMs;
            Gson g = new GsonBuilder().setPrettyPrinting().create();
            try (Writer w = Files.newBufferedWriter(file)) {
                g.toJson(sf, w);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save", e);
        }
    }

    // ── Queries (pull-based hooks + GUI) ─────────────────────────────────────

    public static boolean isActive() {
        return activeName != null && endsAtMs > System.currentTimeMillis();
    }

    public static Modifier current() {
        return isActive() ? Modifier.byName(activeName) : null;
    }

    public static long remainingMs() {
        return isActive() ? Math.max(0L, endsAtMs - System.currentTimeMillis()) : 0L;
    }

    public static long timeUntilNextMs() {
        if (isActive()) return 0L;
        return Math.max(0L, nextEligibleMs - System.currentTimeMillis());
    }

    /** Extra chance (0..0.12) for a loot hook to grant one bonus common drop. */
    public static double extraDropChance() {
        Modifier m = current();
        return m == null ? 0.0 : Math.min(MAX_DROP_BONUS, Math.max(0.0, m.dropBonus));
    }

    /** Coin multiplier (1.0..1.15) a coin/trade hook may honour. */
    public static double coinBonusFactor() {
        Modifier m = current();
        return m == null ? 1.0 : Math.min(MAX_COIN_FACTOR, Math.max(1.0, m.coinFactor));
    }

    public static String statusLine() {
        Modifier m = current();
        if (m != null) {
            return m.colored() + " §7— ends in §f" + PoliticalServer.formatTime(remainingMs());
        }
        return "§8No world modifier. Next possible in §7" + PoliticalServer.formatTime(timeUntilNextMs());
    }

    // ── Tick ─────────────────────────────────────────────────────────────────

    private static int ambientCounter = 0;

    public static void tick(MinecraftServer server) {
        if (server == null) return;
        long now = System.currentTimeMillis();

        if (isActive()) {
            if (++ambientCounter >= 200) {
                ambientCounter = 0;
                ambient(server);
            }
            return;
        }

        // Just ended?
        if (activeName != null && endsAtMs <= now) {
            Modifier ended = Modifier.byName(activeName);
            activeName = null;
            nextEligibleMs = now + MIN_COOLDOWN_MS + (long) (RNG.nextDouble() * (MAX_COOLDOWN_MS - MIN_COOLDOWN_MS));
            if (ended != null) {
                broadcast(server, Component.literal("§7The " + ended.displayName + " has faded from the world."));
            }
            save();
            return;
        }

        // Time to start a new one?
        if (now >= nextEligibleMs) {
            startRandom(server, now);
        }
    }

    private static void startRandom(MinecraftServer server, long now) {
        Modifier[] all = Modifier.values();
        Modifier m = all[RNG.nextInt(all.length)];
        activeName = m.name();
        endsAtMs = now + m.durationMin * 60_000L;
        broadcastStart(server, m);
        save();
    }

    /** Operator/GUI entry: force a specific modifier now. */
    public static void force(MinecraftServer server, Modifier m) {
        if (m == null) return;
        long now = System.currentTimeMillis();
        activeName = m.name();
        endsAtMs = now + m.durationMin * 60_000L;
        broadcastStart(server, m);
        save();
    }

    /** Operator/GUI entry: end the active modifier now. */
    public static void clear(MinecraftServer server) {
        if (!isActive()) return;
        Modifier m = current();
        activeName = null;
        nextEligibleMs = System.currentTimeMillis() + MIN_COOLDOWN_MS;
        if (m != null) broadcast(server, Component.literal("§7The " + m.displayName + " is dispelled."));
        save();
    }

    // ── Flavour ──────────────────────────────────────────────────────────────

    public static void applyModifierFlavour(LivingEntity entity) {
        Modifier m = current();
        if (m == null || m.mobEffect == null) return;
        if (entity == null || !entity.isAlive()) return;
        if (!(entity instanceof Mob mob) || !(entity instanceof Monster)) return;
        if (mob.entityTags().contains(MODIFIER_FLAVOUR_TAG)) return;
        if (mob.level().getRandom().nextFloat() >= 0.15f) return;
        mob.addTag(MODIFIER_FLAVOUR_TAG);
        mob.addEffect(new MobEffectInstance(m.mobEffect, 999999, 0, true, false, false));
    }

    private static void ambient(MinecraftServer server) {
        Modifier m = current();
        if (m == null) return;
        // Fair Winds gives a gentle, hidden, short speed cue under open sky — cosmetic only.
        if (m == Modifier.FAIR_WINDS) {
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                if (!(p.level() instanceof ServerLevel level)) continue;
                if (!level.canSeeSky(p.blockPosition())) continue;
                p.addEffect(new MobEffectInstance(MobEffects.SPEED, 120, 0, true, false, false));
            }
        }
    }

    private static void broadcastStart(MinecraftServer server, Modifier m) {
        Component bar = Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.DARK_GRAY);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(bar);
            p.sendSystemMessage(Component.literal(m.colorCode + "✦ " + m.displayName.toUpperCase() + " ✦")
                    .withStyle(ChatFormatting.BOLD));
            p.sendSystemMessage(Component.literal("§7" + m.flavour));
            p.sendSystemMessage(Component.literal("§8Lasts " + m.durationMin + " min. Open §7/world§8 to see it."));
            p.sendSystemMessage(bar);
            if (p.level() instanceof ServerLevel sl) {
                sl.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                        SoundSource.PLAYERS, 0.8f, 0.9f);
            }
        }
        PoliticalServer.LOGGER.info("[WorldModifiers] {} started.", m.displayName);
    }

    private static void broadcast(MinecraftServer server, Component msg) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(msg);
        }
    }
}
