package com.political;


import com.political.economy.CoinManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Runs incursions: a rift that opens on real ground and becomes whatever that ground deserves.
 *
 * <p>The theme, the roster, the hazard and the boss all come from the biome the rift opened in, so
 * this is content that writes itself against a world nobody authored. Seven waves, escalating, with
 * the last one carrying a boss. Leave the ring and it collapses, which is what stops it being a
 * thing you kite across the map.</p>
 */
public final class IncursionManager {

    private IncursionManager() {}

    public static final String MOB_TAG = "political_incursion";

    private static final Random RNG = new Random();
    private static final int WAVES = 7;
    private static final double RADIUS = 24.0;
    /** How long a wave may drag on before it is cleared out and the next starts. */
    private static final int WAVE_TIMEOUT_TICKS = 20 * 90;

    public static final class Active {
        public final UUID id = UUID.randomUUID();
        public IncursionTheme theme;
        public String dim;
        public BlockPos centre;
        public String ownerUuid;
        public String ownerName;
        public int wave;
        public int ticks;
        public int waveTicks;
        public final List<UUID> living = new ArrayList<>();
        public ServerBossEvent bar;
        public boolean finished;
        public int killed;
    }

    private static final Map<UUID, Active> ACTIVE = new HashMap<>();
    /**
     * When each player may open another.
     *
     * <p>A completed incursion pays fifteen thousand plus a quarter of a thousand per kill, and the
     * only thing stopping a player opening the next one the moment the last sealed was that they
     * could not run two at once. That is an unbounded coin faucet with a twenty minute cycle time.
     * A cooldown makes it an event rather than a job.</p>
     */
    private static final Map<String, Long> NEXT_ALLOWED = new HashMap<>();
    private static final long COOLDOWN_MS = 20 * 60 * 1000L;

    public static java.util.Collection<Active> all() {
        return ACTIVE.values();
    }

    public static int count() {
        return ACTIVE.size();
    }

    /** Whether this player already has one running, since two at once is unmanageable. */
    public static Active activeFor(ServerPlayer player) {
        for (Active a : ACTIVE.values()) {
            if (a.ownerUuid.equals(player.getUUID().toString())) return a;
        }
        return null;
    }

    // ── Opening ───────────────────────────────────────────────────────────

    /** Opens a rift where the player is standing, themed by what is around them. */
    public static boolean open(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return false;
        if (activeFor(player) != null) {
            player.sendSystemMessage(Component.literal("§cYou already have one open."));
            return false;
        }
        Long next = NEXT_ALLOWED.get(player.getUUID().toString());
        if (next != null && System.currentTimeMillis() < next) {
            long mins = Math.max(1, (next - System.currentTimeMillis()) / 60000L);
            player.sendSystemMessage(Component.literal("§cThe air has not settled. About §f"
                    + mins + (mins == 1 ? " minute" : " minutes") + "§c to go."));
            return false;
        }

        BlockPos centre = player.blockPosition();
        String dim = level.dimension().identifier().toString();
        String biome = biomeIdAt(level, centre);
        IncursionTheme theme = IncursionTheme.forLocation(dim, biome, centre.getY());

        Active a = new Active();
        a.theme = theme;
        a.dim = dim;
        a.centre = centre;
        a.ownerUuid = player.getUUID().toString();
        a.ownerName = player.getName().getString();
        a.wave = 0;
        a.bar = new ServerBossEvent(UUID.randomUUID(),
                Component.literal(theme.display).withStyle(theme.colour, ChatFormatting.BOLD),
                barColour(theme), BossEvent.BossBarOverlay.NOTCHED_10);
        a.bar.addPlayer(player);
        a.bar.setProgress(0f);
        ACTIVE.put(a.id, a);

        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("  " + theme.colour + "§l"
                + theme.display.toUpperCase(java.util.Locale.ROOT)));
        player.sendSystemMessage(Component.literal("  §8" + theme.flavor));
        player.sendSystemMessage(Component.literal("  §7" + theme.conditions));
        player.sendSystemMessage(Component.literal("  §7Hazard: " + theme.hazard.colour
                + theme.hazard.label));
        player.sendSystemMessage(Component.literal("  §8Seven waves. Stay inside the ring."));
        player.sendSystemMessage(Component.literal(""));

        level.playSound(null, centre, SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 1.0f, 0.6f);
        Vfx.domainCollapse(level, Vec3.atCenterOf(centre), RADIUS * 0.5, paletteFor(theme));
        startWave(level, a);
        return true;
    }

    private static String biomeIdAt(ServerLevel level, BlockPos pos) {
        try {
            var holder = level.getBiome(pos);
            var key = holder.unwrapKey().orElse(null);
            return key == null ? "" : key.identifier().toString();
        } catch (Exception e) {
            // An unreadable biome is not worth refusing the fight over, it just becomes the default.
            return "";
        }
    }

    private static BossEvent.BossBarColor barColour(IncursionTheme t) {
        return switch (t) {
            case VERDANT -> BossEvent.BossBarColor.GREEN;
            case FROZEN, DROWNED -> BossEvent.BossBarColor.BLUE;
            case SCORCHED, INFERNAL -> BossEvent.BossBarColor.RED;
            case STORMWRACKED -> BossEvent.BossBarColor.YELLOW;
            case VOIDBORNE -> BossEvent.BossBarColor.PURPLE;
            default -> BossEvent.BossBarColor.WHITE;
        };
    }

    private static Vfx.Palette paletteFor(IncursionTheme t) {
        return switch (t) {
            case FROZEN -> Vfx.FROST;
            case SCORCHED, INFERNAL -> Vfx.INFERNO;
            case STORMWRACKED -> Vfx.STORM;
            case VOIDBORNE, HOLLOW -> Vfx.VOID;
            case BLIGHTED -> Vfx.CURSED;
            default -> Vfx.ARCANE;
        };
    }

    // ── Waves ─────────────────────────────────────────────────────────────

    private static void startWave(ServerLevel level, Active a) {
        a.wave++;
        a.waveTicks = 0;
        a.living.clear();

        boolean last = a.wave >= WAVES;
        int count = 3 + a.wave * 2;

        ServerPlayer owner = ownerOf(level.getServer(), a);
        if (owner != null) {
            owner.sendSystemMessage(Component.literal(last
                    ? "  §c§lFINAL WAVE"
                    : "  " + a.theme.colour + "Wave " + a.wave + " §8of " + WAVES));
        }

        for (int i = 0; i < count; i++) {
            EntityType<?> type = a.theme.roster[RNG.nextInt(a.theme.roster.length)];
            spawnOne(level, a, type, false);
        }
        if (last) spawnOne(level, a, a.theme.boss, true);

        a.bar.setProgress(Math.min(1f, (a.wave - 1) / (float) WAVES));
        a.bar.setName(Component.literal(a.theme.display + " §7wave " + a.wave + "/" + WAVES)
                .withStyle(a.theme.colour, ChatFormatting.BOLD));
    }

    private static void spawnOne(ServerLevel level, Active a, EntityType<?> type, boolean boss) {
        BlockPos at = ringPos(level, a);
        if (at == null) return;
        Entity raw = type.spawn(level, at, EntitySpawnReason.EVENT);
        if (!(raw instanceof Mob mob)) {
            if (raw != null) raw.discard();
            return;
        }

        mob.addTag(MOB_TAG);
        mob.setPersistenceRequired();

        // Wave number is the difficulty dial, so late waves are genuinely harder rather than
        // simply more numerous.
        double scale = 1.0 + a.wave * 0.18;
        var hp = mob.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) {
            hp.setBaseValue(hp.getBaseValue() * (boss ? scale * 4.0 : scale));
            mob.setHealth(mob.getMaxHealth());
        }
        var atk = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) atk.setBaseValue(atk.getBaseValue() * (boss ? scale * 1.6 : scale));

        if (boss) {
            mob.setCustomName(Component.literal(a.theme.display.replace(" Incursion", "")
                    + " Sovereign").withStyle(a.theme.colour, ChatFormatting.BOLD));
            mob.setCustomNameVisible(true);
            var kb = mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (kb != null) kb.setBaseValue(0.8);
            var sc = mob.getAttribute(Attributes.SCALE);
            if (sc != null) sc.setBaseValue(1.4);
            Vfx.bloom(level, Vec3.atCenterOf(at));
        }

        a.living.add(mob.getUUID());
        ServerPlayer owner = ownerOf(level.getServer(), a);
        if (owner != null) mob.setTarget(owner);
    }

    /** A spawn point on the edge of the ring, on real ground. */
    private static BlockPos ringPos(ServerLevel level, Active a) {
        for (int attempt = 0; attempt < 16; attempt++) {
            double angle = RNG.nextDouble() * Math.PI * 2;
            double dist = RADIUS * 0.55 + RNG.nextDouble() * RADIUS * 0.35;
            int x = a.centre.getX() + (int) (Math.cos(angle) * dist);
            int z = a.centre.getZ() + (int) (Math.sin(angle) * dist);
            BlockPos ground = level.getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(x, a.centre.getY(), z));
            if (Math.abs(ground.getY() - a.centre.getY()) > 16) continue;
            if (!level.getBlockState(ground).isAir()) continue;
            return ground;
        }
        return a.centre.offset(RNG.nextInt(9) - 4, 1, RNG.nextInt(9) - 4);
    }

    // ── Running ───────────────────────────────────────────────────────────

    public static void tick(MinecraftServer server) {
        if (ACTIVE.isEmpty()) return;

        Iterator<Map.Entry<UUID, Active>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Active a = it.next().getValue();
            ServerLevel level = levelOf(server, a);
            if (level == null) {
                cleanUp(server, a);
                it.remove();
                continue;
            }

            a.ticks++;
            a.waveTicks++;

            ServerPlayer owner = ownerOf(server, a);
            // Nobody home, or they wandered off. Either way it collapses.
            if (owner == null || owner.isDeadOrDying()
                    || owner.distanceToSqr(Vec3.atCenterOf(a.centre)) > (RADIUS * 1.6) * (RADIUS * 1.6)) {
                if (owner != null) {
                    owner.sendSystemMessage(Component.literal(
                            "  §c" + a.theme.display + " collapsed. You left the ring."));
                }
                cleanUp(server, a);
                it.remove();
                continue;
            }

            if (a.ticks % 20 == 0) {
                applyHazard(level, a, owner);
                drawRing(level, a);
            }

            // Prune the dead and count them.
            a.living.removeIf(u -> {
                Entity e = level.getEntity(u);
                if (e == null || !e.isAlive()) {
                    a.killed++;
                    return true;
                }
                return false;
            });

            boolean cleared = a.living.isEmpty();
            boolean timedOut = a.waveTicks > WAVE_TIMEOUT_TICKS;

            if (cleared || timedOut) {
                if (timedOut && !cleared) sweep(level, a);
                if (a.wave >= WAVES) {
                    complete(server, a, owner);
                    it.remove();
                    continue;
                }
                startWave(level, a);
            }
        }
    }

    /** The standing hazard, which is what makes each theme feel like a place rather than a spawner. */
    private static void applyHazard(ServerLevel level, Active a, ServerPlayer owner) {
        switch (a.theme.hazard) {
            case POISON -> owner.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 0, true, false, false));
            case FREEZE -> {
                owner.setTicksFrozen(Math.min(200, owner.getTicksFrozen() + 25));
                owner.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, true, false, false));
            }
            case BURN -> {
                if (a.ticks % 80 == 0) owner.igniteForSeconds(2);
            }
            case DROWN -> owner.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 40, 1, true, false, false));
            case BLIND -> owner.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, true, false, false));
            case PHASE -> {
                if (a.ticks % 100 == 0) owner.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 0));
            }
            case SHOCK -> {
                if (a.ticks % 140 == 0) {
                    var bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.EVENT);
                    if (bolt != null) {
                        Vec3 p = owner.position();
                        bolt.snapTo(p.x + RNG.nextInt(9) - 4, p.y, p.z + RNG.nextInt(9) - 4);
                        level.addFreshEntity(bolt);
                    }
                }
            }
            case WITHER -> owner.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0, true, false, false));
        }
    }

    private static void drawRing(ServerLevel level, Active a) {
        Vec3 c = Vec3.atCenterOf(a.centre);
        for (int i = 0; i < 40; i++) {
            double ang = (Math.PI * 2 / 40) * i;
            double x = c.x + Math.cos(ang) * RADIUS;
            double z = c.z + Math.sin(ang) * RADIUS;
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, c.y + 1, z, 1, 0, 0.2, 0, 0.0);
        }
    }

    /** Clears out whatever is left of a wave that dragged on too long. */
    private static void sweep(ServerLevel level, Active a) {
        for (UUID u : new ArrayList<>(a.living)) {
            Entity e = level.getEntity(u);
            if (e != null) e.discard();
        }
        a.living.clear();
    }

    private static void complete(MinecraftServer server, Active a, ServerPlayer owner) {
        ServerLevel level = levelOf(server, a);
        long reward = 15_000L + a.killed * 250L;

        if (owner != null) {
            CoinManager.giveCoins(owner, reward);
            owner.sendSystemMessage(Component.literal(""));
            owner.sendSystemMessage(Component.literal("  §a§lINCURSION SEALED"));
            owner.sendSystemMessage(Component.literal("  " + a.theme.colour + a.theme.display));
            owner.sendSystemMessage(Component.literal("  §7" + a.killed + " put down"));
            owner.sendSystemMessage(Component.literal("  §7Reward: §6" + GuiKit.num(reward) + " coins"));
            owner.sendSystemMessage(Component.literal(""));
            if (level != null) {
                Vfx.bloom(level, owner.position().add(0, 1, 0));
                level.playSound(null, owner.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                        SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }
        cleanUp(server, a);
    }

    private static void cleanUp(MinecraftServer server, Active a) {
        a.finished = true;
        // Starts on any ending, sealed or collapsed, so walking out of a bad run is not a reset.
        if (a.ownerUuid != null && !a.ownerUuid.isEmpty()) {
            NEXT_ALLOWED.put(a.ownerUuid, System.currentTimeMillis() + COOLDOWN_MS);
        }
        if (a.bar != null) a.bar.removeAllPlayers();
        ServerLevel level = levelOf(server, a);
        if (level != null) sweep(level, a);
    }

    /** Called on shutdown so no incursion mob is left standing in a saved world. */
    public static void shutdown(MinecraftServer server) {
        for (Active a : new ArrayList<>(ACTIVE.values())) cleanUp(server, a);
        ACTIVE.clear();
    }

    private static ServerLevel levelOf(MinecraftServer server, Active a) {
        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension().identifier().toString().equals(a.dim)) return level;
        }
        return null;
    }

    private static ServerPlayer ownerOf(MinecraftServer server, Active a) {
        try {
            return server.getPlayerList().getPlayer(UUID.fromString(a.ownerUuid));
        } catch (Exception e) {
            return null;
        }
    }

    /** How long until this player may open another, in minutes, or zero when ready. */
    public static long cooldownMinutes(ServerPlayer player) {
        Long next = NEXT_ALLOWED.get(player.getUUID().toString());
        if (next == null) return 0;
        long left = next - System.currentTimeMillis();
        return left <= 0 ? 0 : Math.max(1, left / 60000L);
    }

    /** Incursion mobs pay nothing to outside systems, so a rift cannot be farmed for drops. */
    public static boolean isIncursionMob(Entity e) {
        return e != null && e.entityTags().contains(MOB_TAG);
    }
}
