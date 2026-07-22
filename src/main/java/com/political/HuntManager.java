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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * The Great Hunt — a rotating world contract.
 *
 * <p>Every {@link #DEFAULT_COOLDOWN_MS} of real time (default 3 hours) the world produces
 * exactly <b>one</b> named, buffed rare mob somewhere near a random online player. Its
 * approximate location is broadcast. The first player to put it down takes a large coin
 * reward and a trophy; anyone who genuinely helped takes a share of the coins.</p>
 *
 * <h2>Why this cannot be farmed</h2>
 * <ul>
 *   <li><b>One at a time, on a wall-clock cooldown.</b> The payout rate is capped by real
 *       time, not by player effort: eight hunts a day, world-wide, no matter how many people
 *       are online or how long they idle.</li>
 *   <li><b>Contribution-gated.</b> The killer must personally have dealt at least
 *       {@link #KILLER_MIN_CONTRIBUTION} of the target's health (tracked through
 *       {@link BestiaryManager}'s damage ledger) or the kill is treated as a scavenge and
 *       pays a fraction. A player who last-hits someone else's fight does not steal the purse.</li>
 *   <li><b>Assist shares are capped.</b> At most {@link #MAX_ASSISTS} helpers split a fixed
 *       {@link #ASSIST_SHARE} each, and only helpers who dealt real damage qualify. A party
 *       of thirty standing nearby does not multiply the payout.</li>
 *   <li><b>The trophy goes to one player.</b> Trophies never drop more than once per hunt, so
 *       the Grand Trophy Crest genuinely costs four separate hunt victories.</li>
 *   <li><b>No spawn without players.</b> Nothing accrues while the server is empty, and an
 *       unclaimed hunt expires and despawns rather than accumulating.</li>
 * </ul>
 *
 * <p>At the default settings the maximum coin income the hunt system can add to the whole
 * server is roughly 400k per 3 hours — well under a single settlement's steady output, and a
 * rounding error against the 10M cost of a war.</p>
 */
public final class HuntManager {

    private HuntManager() {}

    /** Entity tag identifying a live Great Hunt target. */
    public static final String HUNT_TAG = "political_great_hunt";

    // ── Tunables ────────────────────────────────────────────────────────────

    /** Real time between hunts. */
    public static final long DEFAULT_COOLDOWN_MS = 3L * 60L * 60L * 1000L;
    private static final long MIN_COOLDOWN_MS = 15L * 60L * 1000L;

    /** How long an unclaimed hunt stays in the world before it gives up and leaves. */
    private static final long HUNT_LIFETIME_MS = 45L * 60L * 1000L;

    private static final long BASE_REWARD = 200_000L;
    private static final long REWARD_PER_PRIOR_HUNT = 20_000L;
    private static final long MAX_REWARD = 400_000L;

    /** Share of the target's max health the killer must have dealt for the full purse. */
    private static final double KILLER_MIN_CONTRIBUTION = 0.20;
    /** Multiplier applied when the killer only last-hit someone else's work. */
    private static final double SCAVENGE_MULTIPLIER = 0.25;

    private static final int MAX_ASSISTS = 4;
    private static final double ASSIST_SHARE = 0.15;
    /** Minimum damage an assist must have dealt, as a share of target max health. */
    private static final double ASSIST_MIN_CONTRIBUTION = 0.05;

    /** Horizontal distance band from the anchor player where a hunt spawns. */
    private static final int SPAWN_MIN_RADIUS = 180;
    private static final int SPAWN_MAX_RADIUS = 420;

    private static final int TICK_INTERVAL = 100;

    // ── Archetypes ──────────────────────────────────────────────────────────

    /**
     * One possible Great Hunt. {@code healthMult} and {@code damageMult} scale the vanilla mob
     * into something worth a contract without turning it into a raid boss.
     */
    public record Archetype(String id, String title, String epithet,
                            EntityType<?> type, BestiaryEntry.MobFamily family,
                            double healthMult, double damageMult,
                            String trophyId, net.minecraft.world.item.Item icon,
                            String loreLine) {}

    private static final List<Archetype> ARCHETYPES = List.of(
            new Archetype("thornmaw", "Thornmaw", "the Hollow Beast",
                    EntityType.RAVAGER, BestiaryEntry.MobFamily.ILLAGER,
                    7.0, 1.8, LivingWorldItems.ID_TROPHY_FANG, Items.IRON_AXE,
                    "It has been driven off four times. It has never been killed."),

            new Archetype("grimhold", "Grimhold", "Warden of Ash",
                    EntityType.WITHER_SKELETON, BestiaryEntry.MobFamily.UNDEAD,
                    9.0, 1.9, LivingWorldItems.ID_TROPHY_CROWN, Items.WITHER_SKELETON_SKULL,
                    "Wears a crown it did not earn and will not surrender."),

            new Archetype("cinderjaw", "Cinderjaw", "the Long Burning",
                    EntityType.BLAZE, BestiaryEntry.MobFamily.NETHERBORN,
                    10.0, 1.7, LivingWorldItems.ID_TROPHY_ASHEN, Items.BLAZE_POWDER,
                    "Something opened a door and this walked out of it."),

            new Archetype("tidecaller", "Tidecaller", "of the Drowned Halls",
                    EntityType.ELDER_GUARDIAN, BestiaryEntry.MobFamily.AQUATIC,
                    4.0, 1.6, LivingWorldItems.ID_TROPHY_DEPTHS, Items.PRISMARINE_SHARD,
                    "The wells went salt the week it surfaced."),

            new Archetype("pale_cantor", "The Pale Cantor", "Unbanished",
                    EntityType.EVOKER, BestiaryEntry.MobFamily.ILLAGER,
                    12.0, 1.6, LivingWorldItems.ID_TROPHY_CROWN, Items.TOTEM_OF_UNDYING,
                    "Sings the same seven notes. Nothing that hears them sleeps well."),

            new Archetype("voidstalker", "Voidstalker", "the Patient",
                    EntityType.ENDERMAN, BestiaryEntry.MobFamily.ENDERBORN,
                    8.0, 1.8, LivingWorldItems.ID_TROPHY_DEPTHS, Items.ENDER_EYE,
                    "It has been standing at the edge of the treeline for two days."),

            new Archetype("hollowking", "The Hollow King", "Crowned in Rot",
                    EntityType.ZOMBIE, BestiaryEntry.MobFamily.UNDEAD,
                    14.0, 2.0, LivingWorldItems.ID_TROPHY_FANG, Items.ROTTEN_FLESH,
                    "Marches at the head of nothing at all, and still marches."),

            new Archetype("gravebloom", "Gravebloom", "the Splitting",
                    EntityType.RAVAGER, BestiaryEntry.MobFamily.BEAST,
                    8.0, 1.7, LivingWorldItems.ID_TROPHY_ASHEN, Items.CRIMSON_FUNGUS,
                    "Where it lies down, nothing grows back the same.")
    );

    public static List<Archetype> archetypes() {
        return ARCHETYPES;
    }

    public static Archetype archetypeById(String id) {
        for (Archetype a : ARCHETYPES) {
            if (a.id().equals(id)) return a;
        }
        return null;
    }

    // ── State ───────────────────────────────────────────────────────────────

    /** Gson-friendly snapshot of the one live hunt. */
    public static class ActiveHunt {
        public String archetypeId;
        public String entityUuid;
        public String dimension;
        public int x;
        public int y;
        public int z;
        public long spawnedAtMs;
        public long expiresAtMs;
        public int huntNumber;
        public double maxHealth;

        public ActiveHunt() {}

        public Archetype archetype() {
            return archetypeById(archetypeId);
        }

        public long remainingMs() {
            return Math.max(0L, expiresAtMs - System.currentTimeMillis());
        }
    }

    /** A completed hunt, for the board's history panel. */
    public static class HuntRecord {
        public String archetypeId;
        public String killerUuid;
        public long killedAtMs;
        public long reward;
        public int assists;

        public HuntRecord() {}
    }

    private static class SaveFile {
        ActiveHunt active = null;
        long nextSpawnAtMs = 0L;
        long cooldownMs = DEFAULT_COOLDOWN_MS;
        int huntsCompleted = 0;
        boolean enabled = true;
        List<HuntRecord> history = new ArrayList<>();
    }

    private static ActiveHunt active = null;
    private static long nextSpawnAtMs = 0L;
    private static long cooldownMs = DEFAULT_COOLDOWN_MS;
    private static int huntsCompleted = 0;
    private static boolean enabled = true;
    private static List<HuntRecord> history = new ArrayList<>();

    private static int tickCounter = 0;
    private static Path file;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Random RNG = new Random();

    // ── Persistence ─────────────────────────────────────────────────────────

    public static void initialize(Path worldRoot) {
        file = worldRoot.resolve("living_world_hunts.json");
        if (Files.exists(file)) {
            try (Reader r = Files.newBufferedReader(file)) {
                SaveFile sf = GSON.fromJson(r, new TypeToken<SaveFile>() {}.getType());
                if (sf != null) {
                    active = sf.active;
                    nextSpawnAtMs = sf.nextSpawnAtMs;
                    cooldownMs = Math.max(MIN_COOLDOWN_MS, sf.cooldownMs);
                    huntsCompleted = Math.max(0, sf.huntsCompleted);
                    enabled = sf.enabled;
                    history = sf.history != null ? sf.history : new ArrayList<>();
                }
            } catch (Exception e) {
                PoliticalServer.LOGGER.warn("Failed to load living_world_hunts.json", e);
            }
        }
        if (nextSpawnAtMs <= 0L) {
            // First boot: give the server a short grace period before the first hunt.
            nextSpawnAtMs = System.currentTimeMillis() + 10L * 60L * 1000L;
        }
    }

    public static void save() {
        if (file == null) return;
        try (Writer w = Files.newBufferedWriter(file)) {
            SaveFile sf = new SaveFile();
            sf.active = active;
            sf.nextSpawnAtMs = nextSpawnAtMs;
            sf.cooldownMs = cooldownMs;
            sf.huntsCompleted = huntsCompleted;
            sf.enabled = enabled;
            // Keep the history bounded so the file cannot grow without limit.
            sf.history = history.size() > 25 ? new ArrayList<>(history.subList(history.size() - 25, history.size()))
                                             : history;
            GSON.toJson(sf, w);
        } catch (Exception e) {
            PoliticalServer.LOGGER.warn("Failed to save living_world_hunts.json", e);
        }
    }

    // ── Queries used by the GUI/commands ────────────────────────────────────

    public static ActiveHunt active() {
        return active;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static int huntsCompleted() {
        return huntsCompleted;
    }

    public static long cooldownMs() {
        return cooldownMs;
    }

    public static long msUntilNextHunt() {
        return Math.max(0L, nextSpawnAtMs - System.currentTimeMillis());
    }

    public static List<HuntRecord> history() {
        List<HuntRecord> copy = new ArrayList<>(history);
        java.util.Collections.reverse(copy);
        return copy;
    }

    /** Reward the current (or next) hunt would pay to its killer. */
    public static long currentReward() {
        return Math.min(MAX_REWARD, BASE_REWARD + REWARD_PER_PRIOR_HUNT * huntsCompleted);
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        save();
    }

    public static void setCooldownMs(long ms) {
        cooldownMs = Math.max(MIN_COOLDOWN_MS, ms);
        save();
    }

    /** Admin: force the next hunt to start now. */
    public static void forceSpawn(MinecraftServer server) {
        if (active != null) despawn(server, false);
        nextSpawnAtMs = 0L;
        trySpawn(server);
    }

    // ── Tick ────────────────────────────────────────────────────────────────

    public static void tick(MinecraftServer server) {
        if (server == null || !enabled) return;
        if (++tickCounter < TICK_INTERVAL) return;
        tickCounter = 0;

        long now = System.currentTimeMillis();

        if (active != null) {
            if (now >= active.expiresAtMs) {
                despawn(server, true);
            } else {
                // If the target was removed by some other means (chunk purge, /kill), clear it
                // so the next hunt is not blocked forever.
                ServerLevel level = levelOf(server, active.dimension);
                if (level != null && level.isLoaded(new BlockPos(active.x, active.y, active.z))) {
                    Entity e = level.getEntity(UUID.fromString(active.entityUuid));
                    if (e == null || !e.isAlive()) {
                        clearActive(server, false);
                    }
                }
            }
            return;
        }

        if (now >= nextSpawnAtMs) {
            trySpawn(server);
        }
    }

    // ── Spawning ────────────────────────────────────────────────────────────

    private static void trySpawn(MinecraftServer server) {
        List<ServerPlayer> candidates = new ArrayList<>();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (p.isSpectator()) continue;
            if (p.level() instanceof ServerLevel) candidates.add(p);
        }
        if (candidates.isEmpty()) {
            // Nobody to hunt for. Re-check in a minute; the cooldown does not "bank up".
            nextSpawnAtMs = System.currentTimeMillis() + 60_000L;
            return;
        }

        ServerPlayer anchor = candidates.get(RNG.nextInt(candidates.size()));
        ServerLevel level = (ServerLevel) anchor.level();

        Archetype arch = ARCHETYPES.get(RNG.nextInt(ARCHETYPES.size()));

        double angle = RNG.nextDouble() * Math.PI * 2.0;
        int radius = SPAWN_MIN_RADIUS + RNG.nextInt(SPAWN_MAX_RADIUS - SPAWN_MIN_RADIUS + 1);
        int x = (int) Math.round(anchor.getX() + Math.cos(angle) * radius);
        int z = (int) Math.round(anchor.getZ() + Math.sin(angle) * radius);
        int y;
        try {
            BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(x, (int) anchor.getY(), z));
            y = ground.getY() + 1;
        } catch (Throwable t) {
            y = (int) anchor.getY();
        }
        if (y < level.getMinY() + 2) y = (int) anchor.getY();

        BlockPos pos = new BlockPos(x, y, z);

        Entity spawned;
        try {
            spawned = arch.type().spawn(level, pos, EntitySpawnReason.EVENT);
        } catch (Throwable t) {
            PoliticalServer.LOGGER.warn("[Hunt] Failed to spawn {}: {}", arch.id(), t.toString());
            nextSpawnAtMs = System.currentTimeMillis() + 5L * 60_000L;
            return;
        }
        if (!(spawned instanceof Mob mob)) {
            nextSpawnAtMs = System.currentTimeMillis() + 5L * 60_000L;
            return;
        }

        double maxHealth = dressUp(mob, arch);

        long now = System.currentTimeMillis();
        active = new ActiveHunt();
        active.archetypeId = arch.id();
        active.entityUuid = mob.getUUID().toString();
        active.dimension = level.dimension().identifier().toString();
        active.x = x;
        active.y = y;
        active.z = z;
        active.spawnedAtMs = now;
        active.expiresAtMs = now + HUNT_LIFETIME_MS;
        active.huntNumber = huntsCompleted + 1;
        active.maxHealth = maxHealth;
        save();

        announceSpawn(server, arch);
    }

    /** Applies the buffs, name and tags. Returns the resulting max health. */
    private static double dressUp(Mob mob, Archetype arch) {
        mob.addTag(HUNT_TAG);
        mob.addTag("political_hunt_" + arch.id());
        mob.setPersistenceRequired();

        double maxHealth = 20.0;
        var health = mob.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            maxHealth = health.getBaseValue() * arch.healthMult();
            health.setBaseValue(maxHealth);
            mob.setHealth((float) maxHealth);
        }
        var attack = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            attack.setBaseValue(attack.getBaseValue() * arch.damageMult());
        }
        var knockbackResist = mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (knockbackResist != null) {
            knockbackResist.setBaseValue(Math.min(1.0, knockbackResist.getBaseValue() + 0.6));
        }

        mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 999999, 0, true, false, false));
        mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 999999, 0, true, false, false));

        mob.setCustomName(Component.literal("✦ " + arch.title() + ", " + arch.epithet())
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        mob.setCustomNameVisible(true);
        return maxHealth;
    }

    // ── Announcements ───────────────────────────────────────────────────────

    private static void announceSpawn(MinecraftServer server, Archetype arch) {
        String hint = locationHint();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.literal("═══════════════════════════════════")
                    .withStyle(ChatFormatting.DARK_RED));
            p.sendSystemMessage(Component.literal("✦ THE GREAT HUNT ✦")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            p.sendSystemMessage(Component.literal("§f" + arch.title() + "§7, " + arch.epithet()
                    + " §8walks."));
            p.sendSystemMessage(Component.literal("§7" + arch.loreLine()));
            p.sendSystemMessage(Component.literal("§7Last seen: §f" + hint));
            p.sendSystemMessage(Component.literal("§7Bounty: §6" + fmt(currentReward())
                    + " coins §7+ a trophy"));
            p.sendSystemMessage(Component.literal("§8First to fell it takes the purse. §7/hunt"));
            p.sendSystemMessage(Component.literal("═══════════════════════════════════")
                    .withStyle(ChatFormatting.DARK_RED));

            if (p.level() instanceof ServerLevel sl) {
                sl.playSound(null, p.blockPosition(), SoundEvents.RAID_HORN.value(),
                        SoundSource.PLAYERS, 0.8f, 0.9f);
            }
        }
        PoliticalServer.LOGGER.info("[Hunt] Spawned {} at {} {} {} in {}",
                arch.title(), active.x, active.y, active.z, active.dimension);
    }

    /**
     * A deliberately fuzzy location: coordinates rounded to the nearest 100 blocks, so the
     * hunt has to actually be found rather than teleported to.
     */
    public static String locationHint() {
        if (active == null) return "unknown";
        int rx = Math.round(active.x / 100f) * 100;
        int rz = Math.round(active.z / 100f) * 100;
        String dim = active.dimension;
        int colon = dim.indexOf(':');
        if (colon >= 0) dim = dim.substring(colon + 1);
        return "around " + rx + ", " + rz + " §8(±100) §7in §f" + dim;
    }

    // ── Death handling ──────────────────────────────────────────────────────

    /** Returns true if this death was the active Great Hunt. */
    public static boolean isHuntTarget(Entity entity) {
        if (entity == null) return false;
        return entity.entityTags().contains(HUNT_TAG);
    }

    /**
     * Called from the death listener for every non-player death. Resolves the hunt if the
     * dead entity was the active target.
     */
    public static void onEntityDeath(MinecraftServer server, LivingEntity victim, Entity killerEntity) {
        if (active == null || victim == null) return;
        if (!isHuntTarget(victim)) return;
        if (!victim.getUUID().toString().equals(active.entityUuid)) {
            // A stale hunt target from a previous cycle. Nothing to pay out.
            return;
        }

        Archetype arch = active.archetype();
        Map<String, Float> contributions = BestiaryManager.contributions(victim.getUUID());
        double maxHealth = active.maxHealth > 0 ? active.maxHealth : 20.0;

        ServerPlayer killer = killerEntity instanceof ServerPlayer sp ? sp : null;
        if (killer == null) {
            // Died to the world (fall, lava, another mob). Nobody is paid; the contract lapses.
            broadcast(server, Component.literal("§8The Great Hunt fell to no one. The bounty lapses."));
            clearActive(server, true);
            return;
        }

        long purse = currentReward();
        float killerDealt = contributions.getOrDefault(killer.getUUID().toString(), 0f);
        boolean scavenged = killerDealt < maxHealth * KILLER_MIN_CONTRIBUTION;
        if (scavenged) purse = Math.round(purse * SCAVENGE_MULTIPLIER);

        CoinManager.giveCoinsQuiet(killer, purse);

        // Trophy: exactly one, to the killer, and only on a genuine kill.
        String trophyId = arch != null ? arch.trophyId() : LivingWorldItems.ID_TROPHY_FANG;
        if (!scavenged) {
            ItemStack trophy = LivingWorldItems.createTrophy(trophyId, 1);
            LivingWorldItems.give(killer, trophy);
        }

        // Assists: capped in both count and share, and only for real damage.
        List<String> paidAssists = payAssists(server, killer, contributions, maxHealth, purse);

        HuntRecord record = new HuntRecord();
        record.archetypeId = active.archetypeId;
        record.killerUuid = killer.getUUID().toString();
        record.killedAtMs = System.currentTimeMillis();
        record.reward = purse;
        record.assists = paidAssists.size();
        history.add(record);

        huntsCompleted++;

        announceKill(server, arch, killer, purse, scavenged, paidAssists.size(), trophyId);
        BestiaryManager.forgetEntity(victim.getUUID());
        clearActive(server, true);
    }

    private static List<String> payAssists(MinecraftServer server, ServerPlayer killer,
                                           Map<String, Float> contributions, double maxHealth,
                                           long purse) {
        List<String> paid = new ArrayList<>();
        if (contributions.isEmpty()) return paid;

        // Order helpers by damage dealt so the biggest contributors get the capped slots.
        List<Map.Entry<String, Float>> ordered = new ArrayList<>(contributions.entrySet());
        ordered.sort((a, b) -> Float.compare(b.getValue(), a.getValue()));

        long share = Math.round(purse * ASSIST_SHARE);
        if (share <= 0) return paid;

        for (Map.Entry<String, Float> e : ordered) {
            if (paid.size() >= MAX_ASSISTS) break;
            String uuid = e.getKey();
            if (uuid.equals(killer.getUUID().toString())) continue;
            if (e.getValue() < maxHealth * ASSIST_MIN_CONTRIBUTION) continue;

            ServerPlayer helper;
            try {
                helper = server.getPlayerList().getPlayer(UUID.fromString(uuid));
            } catch (Exception ex) {
                continue;
            }
            if (helper == null) continue;

            CoinManager.giveCoinsQuiet(helper, share);
            helper.sendSystemMessage(Component.literal("§6+" + fmt(share)
                    + " coins §7for your part in the Great Hunt."));
            paid.add(uuid);
        }
        return paid;
    }

    private static void announceKill(MinecraftServer server, Archetype arch, ServerPlayer killer,
                                     long purse, boolean scavenged, int assists, String trophyId) {
        String title = arch != null ? arch.title() : "The Great Hunt";
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.literal("═══════════════════════════════════")
                    .withStyle(ChatFormatting.GOLD));
            p.sendSystemMessage(Component.literal("✦ THE HUNT IS OVER ✦")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            p.sendSystemMessage(Component.literal("§f" + title + " §7was felled by §e"
                    + killer.getName().getString()));
            p.sendSystemMessage(Component.literal("§7Purse: §6" + fmt(purse) + " coins"
                    + (scavenged ? " §8(scavenged — reduced)" : "")));
            if (!scavenged) {
                p.sendSystemMessage(Component.literal("§7Trophy: §d"
                        + LivingWorldItems.trophyDisplayName(trophyId)));
            }
            if (assists > 0) {
                p.sendSystemMessage(Component.literal("§7Assists paid: §f" + assists));
            }
            p.sendSystemMessage(Component.literal("§8Next hunt in "
                    + PoliticalServer.formatTime(cooldownMs) + "."));
            p.sendSystemMessage(Component.literal("═══════════════════════════════════")
                    .withStyle(ChatFormatting.GOLD));

            if (p.level() instanceof ServerLevel sl) {
                sl.playSound(null, p.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                        SoundSource.PLAYERS, 0.7f, 1.0f);
            }
        }
    }

    // ── Cleanup ─────────────────────────────────────────────────────────────

    private static void despawn(MinecraftServer server, boolean announce) {
        if (active == null) return;
        ServerLevel level = levelOf(server, active.dimension);
        if (level != null) {
            try {
                Entity e = level.getEntity(UUID.fromString(active.entityUuid));
                if (e != null) e.discard();
            } catch (Exception ignored) {
                // Malformed uuid from a hand-edited save; nothing to discard.
            }
        }
        if (announce) {
            broadcast(server, Component.literal("§8The Great Hunt slipped away unclaimed."));
        }
        clearActive(server, true);
    }

    private static void clearActive(MinecraftServer server, boolean startCooldown) {
        active = null;
        if (startCooldown) {
            nextSpawnAtMs = System.currentTimeMillis() + cooldownMs;
        }
        save();
    }

    private static ServerLevel levelOf(MinecraftServer server, String dimensionId) {
        if (server == null || dimensionId == null) return null;
        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension().identifier().toString().equals(dimensionId)) return level;
        }
        return null;
    }

    private static void broadcast(MinecraftServer server, Component msg) {
        if (server == null) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(msg);
        }
    }

    /** Comma-grouped number, for chat and lore. */
    public static String fmt(long value) {
        return String.format(java.util.Locale.ROOT, "%,d", value);
    }

    /** Snapshot of the current hunt's health, for the GUI. Returns -1 when unknown. */
    public static double currentHealth(MinecraftServer server) {
        if (active == null || server == null) return -1;
        ServerLevel level = levelOf(server, active.dimension);
        if (level == null) return -1;
        try {
            Entity e = level.getEntity(UUID.fromString(active.entityUuid));
            if (e instanceof LivingEntity le) return le.getHealth();
        } catch (Exception ignored) {
            return -1;
        }
        return -1;
    }

    /** Ordered map of archetype id -> how many times it has been felled. */
    public static Map<String, Integer> killsByArchetype() {
        Map<String, Integer> out = new LinkedHashMap<>();
        for (Archetype a : ARCHETYPES) out.put(a.id(), 0);
        for (HuntRecord r : history) {
            out.merge(r.archetypeId, 1, Integer::sum);
        }
        return out;
    }
}
