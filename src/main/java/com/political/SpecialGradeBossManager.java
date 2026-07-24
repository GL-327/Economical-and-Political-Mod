package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * V6.4–V6.5 — the Special Grade boss fight engine (static).
 *
 * <p>A Cursed Womb ritual (see {@link BossRewards}) plus coins summons a chosen {@link SpecialGradeBoss}.
 * The fight is multi-phase with a {@link ServerBossEvent} bar and telegraphed, mechanic-specific
 * attacks. Phase transitions at 66% and 33% health each fire a brief <b>heal-lock</b> (the boss is
 * invulnerable while it channels), a <b>domain expansion</b> (a themed arena where it hits harder
 * and applies a signature effect), and an <b>enrage</b>. A fair kill pays coins, a boss-only
 * signature relic, and a trophy.
 *
 * <p><b>Anti-exploit.</b> Summoning costs a crafted Cursed Womb <i>plus</i> coins (12k–25k tier),
 * and every player is on a per-summon cooldown scaled by boss (45–90 min). Rewards only pay on a
 * real kill of a hard fight, are not time-based, and cannot be farmed off an idle mob (bosses have
 * a hard lifespan and abandon cleanly when everyone leaves). Nothing here pays for elapsed time.
 *
 * <p><b>Restart safety.</b> Active bosses and their domains are persisted; on boot the stale
 * entities are queued for removal and {@link DomainManager} reverts every arena. Kill counts and
 * cooldowns survive restarts.
 */
public final class SpecialGradeBossManager {

    private SpecialGradeBossManager() {}

    public static final String BOSS_TAG = "political_sgb";
    public static final String ADD_TAG = "political_sgb_add";

    public static String bossTag(SpecialGradeBoss boss) {
        return "political_sgb_" + boss.id;
    }

    private static final int MAX_ACTIVE = 3;
    private static final int MAX_ADDS = 8;
    private static final int DOMAIN_RADIUS = 12;
    private static final long DOMAIN_DURATION_MS = 45_000L;
    private static final long HEAL_LOCK_MS = 3_000L;
    private static final long MAX_LIFE_MS = 20 * 60_000L;   // hard lifespan
    private static final long ABANDON_GRACE_MS = 40_000L;   // entity missing this long with nobody near => cleanup
    private static final double PARTICIPANT_RANGE = 28.0;

    private static final Random RNG = new Random();

    // ════════════════════════════════════════════════════════════
    // STATE
    // ════════════════════════════════════════════════════════════

    /** entityUuid -> live fight. */
    private static final Map<UUID, ActiveBoss> ACTIVE = new ConcurrentHashMap<>();
    /** playerUuid -> epoch millis the next summon is allowed. */
    private static final Map<String, Long> COOLDOWNS = new ConcurrentHashMap<>();
    /** playerUuid -> bossId -> kills. */
    private static final Map<String, Map<String, Integer>> KILLS = new ConcurrentHashMap<>();

    /** Stale bosses recovered from disk; entities removed on the first tick with a level. */
    private static final List<String[]> PENDING_CLEANUP = new ArrayList<>(); // {dim, entityUuid}

    private static Path dataFile;
    private static int tickCounter = 0;

    private static final class ActiveBoss {
        final UUID entityUuid;
        final SpecialGradeBoss boss;
        final String dim;
        final BlockPos arena;
        final String summonerUuid;
        final ServerBossEvent bar;
        final long spawnedAtMs;
        final Set<UUID> participants = ConcurrentHashMap.newKeySet();

        int phase = 0;
        long transitionUntilMs = 0L;
        double lockHealth = 0.0;
        boolean enraged = false;
        boolean transformed = false;
        double lastHealth;
        long lastSeenAliveMs;
        long nextAbilityMs;
        long windupUntilMs = 0L;
        int pendingAbility = -1;
        long counterReadyMs = 0L;
        long lastSignatureMs = 0L;
        boolean rewarded = false;

        ActiveBoss(UUID entityUuid, SpecialGradeBoss boss, String dim, BlockPos arena,
                   String summonerUuid, ServerBossEvent bar) {
            this.entityUuid = entityUuid;
            this.boss = boss;
            this.dim = dim;
            this.arena = arena;
            this.summonerUuid = summonerUuid;
            this.bar = bar;
            this.spawnedAtMs = System.currentTimeMillis();
            this.lastSeenAliveMs = spawnedAtMs;
            this.lastHealth = boss.baseHealth;
            this.nextAbilityMs = spawnedAtMs + 6_000L;
        }
    }

    // ════════════════════════════════════════════════════════════
    // PERSISTENCE
    // ════════════════════════════════════════════════════════════

    private static class BossStub {
        String entityUuid;
        String dim;
    }

    private static class SaveBlob {
        List<BossStub> active = new ArrayList<>();
        Map<String, Long> cooldowns = new HashMap<>();
        Map<String, Map<String, Integer>> kills = new HashMap<>();
    }

    public static void initialize(Path worldRoot) {
        dataFile = worldRoot.resolve("special_grade_bosses.json");
        ACTIVE.clear();
        COOLDOWNS.clear();
        KILLS.clear();
        PENDING_CLEANUP.clear();
        tickCounter = 0;

        if (Files.exists(dataFile)) {
            try (Reader r = Files.newBufferedReader(dataFile)) {
                Gson g = new GsonBuilder().create();
                Type t = new TypeToken<SaveBlob>() {}.getType();
                SaveBlob blob = g.fromJson(r, t);
                if (blob != null) {
                    if (blob.cooldowns != null) COOLDOWNS.putAll(blob.cooldowns);
                    if (blob.kills != null) KILLS.putAll(blob.kills);
                    if (blob.active != null) {
                        for (BossStub s : blob.active) {
                            if (s != null && s.entityUuid != null && s.dim != null) {
                                PENDING_CLEANUP.add(new String[]{s.dim, s.entityUuid});
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[SpecialGradeBossManager] Failed to load: " + e.getMessage());
            }
        }
        save();
    }

    public static void save() {
        if (dataFile == null) return;
        try {
            Files.createDirectories(dataFile.getParent());
            SaveBlob blob = new SaveBlob();
            for (ActiveBoss a : ACTIVE.values()) {
                BossStub s = new BossStub();
                s.entityUuid = a.entityUuid.toString();
                s.dim = a.dim;
                blob.active.add(s);
            }
            blob.cooldowns = new LinkedHashMap<>(COOLDOWNS);
            blob.kills = new LinkedHashMap<>(KILLS);
            Gson g = new GsonBuilder().setPrettyPrinting().create();
            try (Writer w = Files.newBufferedWriter(dataFile)) {
                g.toJson(blob, w);
            }
        } catch (Exception e) {
            System.err.println("[SpecialGradeBossManager] Failed to save: " + e.getMessage());
        }
    }

    /** SERVER_STOPPING: discard every boss + add, drop every bar, revert every domain. */
    public static void shutdown(MinecraftServer server) {
        for (ActiveBoss a : new ArrayList<>(ACTIVE.values())) {
            ServerLevel level = levelFor(server, a.dim);
            if (level != null) {
                Entity e = level.getEntity(a.entityUuid);
                if (e != null) e.discard();
                clearAdds(level, a);
                DomainManager.revert(level, a.entityUuid);
            }
            a.bar.removeAllPlayers();
        }
        ACTIVE.clear();
        save();
    }

    // ════════════════════════════════════════════════════════════
    // SUMMON
    // ════════════════════════════════════════════════════════════

    /** Human-readable reason the player cannot summon this boss, or null if they can. */
    public static String blockReason(ServerPlayer player, SpecialGradeBoss boss) {
        if (boss == null) return "Unknown boss.";
        long cd = cooldownRemaining(player.getUUID().toString());
        if (cd > 0) return "On cooldown for " + PoliticalServer.formatTime(cd) + ".";
        if (ACTIVE.size() >= MAX_ACTIVE) return "Too many Special Grade curses are already loose.";
        if (BossRewards.countWombs(player) < 1) return "Requires a Cursed Womb.";
        if (CoinManager.getCoinsLong(player) < boss.summonCoinCost) {
            return "Costs " + String.format("%,d", boss.summonCoinCost) + " coins.";
        }
        return null;
    }

    public static boolean summon(ServerPlayer player, SpecialGradeBoss boss) {
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        if (server == null) return false;

        String block = blockReason(player, boss);
        if (block != null) {
            player.sendSystemMessage(Component.literal("§c" + block));
            return false;
        }

        ServerLevel level = (ServerLevel) player.level();

        // Charge the ritual: consume the Womb FIRST, then coins. If coins fail, refund the Womb.
        if (!BossRewards.consumeOneWomb(player)) {
            player.sendSystemMessage(Component.literal("§cRequires a Cursed Womb."));
            return false;
        }
        if (!CoinManager.removeCoins(player, boss.summonCoinCost)) {
            giveOrDrop(player, BossRewards.createCursedWomb(1));
            player.sendSystemMessage(Component.literal("§cYou cannot afford the "
                    + String.format("%,d", boss.summonCoinCost) + " coin ritual."));
            return false;
        }

        // Find a surface a few blocks from the player.
        BlockPos base = player.blockPosition();
        int ox = 5 + RNG.nextInt(3);
        int oz = 5 + RNG.nextInt(3);
        if (RNG.nextBoolean()) ox = -ox;
        if (RNG.nextBoolean()) oz = -oz;
        BlockPos spawnPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,
                base.offset(ox, 0, oz));

        Mob mob = boss.spawnEntity(level, spawnPos);
        if (mob == null) {
            // Refund everything on a spawn failure.
            giveOrDrop(player, BossRewards.createCursedWomb(1));
            CoinManager.giveCoinsQuiet(player, boss.summonCoinCost);
            player.sendSystemMessage(Component.literal("§cThe ritual failed to take hold. Refunded."));
            return false;
        }
        level.addFreshEntity(mob);

        ServerBossEvent bar = new ServerBossEvent(java.util.UUID.randomUUID(),
                Component.literal(boss.displayName()).withStyle(boss.color, ChatFormatting.BOLD),
                boss.barColor, net.minecraft.world.BossEvent.BossBarOverlay.NOTCHED_10);
        bar.setProgress(1.0f);
        bar.addPlayer(player);

        ActiveBoss ab = new ActiveBoss(mob.getUUID(), boss, level.dimension().identifier().toString(),
                spawnPos, player.getUUID().toString(), bar);
        ab.participants.add(player.getUUID());
        ACTIVE.put(mob.getUUID(), ab);

        // Per-player cooldown starts now.
        COOLDOWNS.put(player.getUUID().toString(),
                System.currentTimeMillis() + boss.cooldownMsForSummoner());

        announceSummon(level, boss, spawnPos, player);
        save();
        return true;
    }

    private static void announceSummon(ServerLevel level, SpecialGradeBoss boss, BlockPos pos, ServerPlayer summoner) {
        level.playSound(null, pos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.2f, 0.5f);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                60, 1.5, 1.5, 1.5, 0.05);
        Component msg = Component.literal("§4§l☠ SPECIAL GRADE ☠ §r")
                .append(Component.literal(boss.displayName()).withStyle(boss.color, ChatFormatting.BOLD))
                .append(Component.literal(" §7has been summoned by §f" + summoner.getName().getString()));
        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            p.sendSystemMessage(msg);
        }
    }

    // ════════════════════════════════════════════════════════════
    // TICK
    // ════════════════════════════════════════════════════════════

    public static void tick(MinecraftServer server) {
        if (server == null) return;
        tickCounter++;
        if (tickCounter % 20 != 0) return; // once a second

        // Restart cleanup: remove stale boss entities recovered from disk.
        if (!PENDING_CLEANUP.isEmpty()) {
            for (int i = PENDING_CLEANUP.size() - 1; i >= 0; i--) {
                String[] rec = PENDING_CLEANUP.get(i);
                ServerLevel level = levelFor(server, rec[0]);
                if (level == null) continue;
                try {
                    Entity e = level.getEntity(UUID.fromString(rec[1]));
                    if (e != null) e.discard();
                } catch (IllegalArgumentException ignored) {}
                PENDING_CLEANUP.remove(i);
            }
        }

        if (ACTIVE.isEmpty()) return;
        long now = System.currentTimeMillis();
        for (ActiveBoss a : new ArrayList<>(ACTIVE.values())) {
            try {
                tickBoss(server, a, now);
            } catch (Exception e) {
                System.err.println("[SpecialGradeBossManager] Boss " + a.boss.id + " errored: " + e.getMessage());
                cleanupNoReward(server, a);
            }
        }
    }

    private static void tickBoss(MinecraftServer server, ActiveBoss a, long now) {
        ServerLevel level = levelFor(server, a.dim);
        if (level == null) return;

        Entity ent = level.getEntity(a.entityUuid);

        // Entity missing (dead & removed, or unloaded).
        if (!(ent instanceof LivingEntity boss) || !boss.isAlive()) {
            boolean anyoneNear = false;
            for (ServerPlayer p : level.players()) {
                if (p.blockPosition().distSqr(a.arena) <= 64 * 64) { anyoneNear = true; break; }
            }
            // If it's been gone a while and nobody is around, abandon cleanly (no reward).
            if (now - a.lastSeenAliveMs > ABANDON_GRACE_MS && !anyoneNear) {
                cleanupNoReward(server, a);
            }
            return;
        }
        a.lastSeenAliveMs = now;

        // Hard lifespan: the curse recedes, no reward, everything cleaned up.
        if (now - a.spawnedAtMs > MAX_LIFE_MS) {
            for (ServerPlayer p : level.players()) {
                if (p.blockPosition().distSqr(a.arena) <= 96 * 96) {
                    p.sendSystemMessage(Component.literal("§8The curse of " + a.boss.title
                            + " recedes into the earth."));
                }
            }
            boss.discard();
            cleanupNoReward(server, a);
            return;
        }

        // Everyone offline / far and long idle handled above; keep the target fresh.
        ServerPlayer nearest = nearestPlayer(level, boss, 48.0);
        if (boss instanceof Mob mob && nearest != null) {
            mob.setTarget(nearest);
        }

        double max = Math.max(1.0, boss.getMaxHealth());
        double hp = boss.getHealth();
        double frac = hp / max;

        // ── phase transitions ──
        if (a.phase < 1 && frac <= 0.66) startTransition(level, a, boss, 1, max);
        else if (a.phase < 2 && frac <= 0.33) startTransition(level, a, boss, 2, max);

        boolean channelling = now < a.transitionUntilMs;
        if (channelling) {
            // Heal-lock: hold the boss at the phase threshold — incoming damage is negated.
            if (boss.getHealth() < a.lockHealth) boss.setHealth((float) a.lockHealth);
            level.sendParticles(ParticleTypes.ENCHANT, boss.getX(), boss.getY() + 1.2, boss.getZ(),
                    20, 0.8, 1.0, 0.8, 0.4);
        } else {
            // Counter mechanic: a big chunk of damage since last tick provokes a reversal.
            if (a.boss.mechanic == SpecialGradeBoss.Mechanic.COUNTER
                    && now >= a.counterReadyMs
                    && a.lastHealth - boss.getHealth() > max * 0.05) {
                reversalShockwave(level, a, boss);
                a.counterReadyMs = now + 6_000L;
            }

            // Passive regeneration for the regenerator (faster inside its own domain).
            if (a.boss.mechanic == SpecialGradeBoss.Mechanic.REGENERATOR) {
                double regen = DomainManager.hasDomain(a.entityUuid) ? max * 0.02 : max * 0.01;
                boss.heal((float) Math.min(regen, max - boss.getHealth()));
                if (boss.getHealth() < max) {
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, boss.getX(), boss.getY() + 1.5, boss.getZ(),
                            4, 0.6, 0.6, 0.6, 0.05);
                }
            }

            // Telegraphed ability windup / execute.
            handleAbilities(level, a, boss, now, max);
        }

        a.lastHealth = boss.getHealth();

        // ── domain: signature effect + bar upkeep ──
        applyDomainEffects(level, a, boss, now);

        // ── bar upkeep ──
        a.bar.setName(Component.literal(a.boss.displayName()
                + (a.enraged ? " §c[ENRAGED]" : "")).withStyle(a.boss.color, ChatFormatting.BOLD));
        a.bar.setProgress((float) Math.max(0.0, Math.min(1.0, boss.getHealth() / max)));
        for (ServerPlayer p : level.players()) {
            boolean near = p.blockPosition().distSqr(boss.blockPosition()) <= 64 * 64;
            boolean has = a.bar.getPlayers().contains(p);
            if (near && !has) a.bar.addPlayer(p);
            else if (!near && has) a.bar.removePlayer(p);
            if (near) a.participants.add(p.getUUID());
        }
    }

    // ════════════════════════════════════════════════════════════
    // PHASES
    // ════════════════════════════════════════════════════════════

    private static void startTransition(ServerLevel level, ActiveBoss a, LivingEntity boss, int phase, double max) {
        a.phase = phase;
        a.transitionUntilMs = System.currentTimeMillis() + HEAL_LOCK_MS;
        a.lockHealth = phase == 1 ? max * 0.66 : max * 0.33;
        if (boss.getHealth() < a.lockHealth) boss.setHealth((float) a.lockHealth);

        // Expand the domain around the arena.
        DomainExpansion.Blueprint bp = new DomainExpansion.Blueprint(
                a.entityUuid, a.dim, boss.blockPosition(), DOMAIN_RADIUS, a.boss.theme, DOMAIN_DURATION_MS);
        DomainManager.expand(level, bp);

        level.playSound(null, boss.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.5f, 0.7f);
        level.sendParticles(ParticleTypes.SONIC_BOOM, boss.getX(), boss.getY() + 1, boss.getZ(),
                3, 0.1, 0.1, 0.1, 0.0);

        String head = phase == 1 ? "§4§lDOMAIN EXPANSION" : "§4§lDOMAIN EXPANSION — ENRAGE";
        for (ServerPlayer p : level.players()) {
            if (p.blockPosition().distSqr(boss.blockPosition()) <= 96 * 96) {
                p.sendSystemMessage(Component.literal(head + " §r"
                        + a.boss.color + "\"" + a.boss.theme.title + "\""));
                p.sendSystemMessage(Component.literal("§7" + a.boss.theme.flavor));
                p.sendSystemMessage(Component.literal("§8The boss channels — invulnerable for a moment."));
            }
        }

        if (phase == 2) {
            a.enraged = true;
            boss.addEffect(new MobEffectInstance(MobEffects.STRENGTH, Integer.MAX_VALUE, 1, false, false, false));
            boss.addEffect(new MobEffectInstance(MobEffects.SPEED, Integer.MAX_VALUE, 0, false, false, false));

            // Transformer: reborn stronger.
            if (a.boss.mechanic == SpecialGradeBoss.Mechanic.TRANSFORMER && !a.transformed) {
                a.transformed = true;
                var atk = boss.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                if (atk != null) atk.setBaseValue(atk.getBaseValue() * 1.5);
                boss.setCustomName(Component.literal(a.boss.color + "" + ChatFormatting.BOLD
                        + a.boss.icon + " " + a.boss.title + " §c§l(REBORN)"));
                boss.addEffect(new MobEffectInstance(MobEffects.STRENGTH, Integer.MAX_VALUE, 2, false, false, false));
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, boss.getX(), boss.getY() + 1, boss.getZ(),
                        80, 1.0, 1.5, 1.0, 0.1);
                for (ServerPlayer p : level.players()) {
                    if (p.blockPosition().distSqr(boss.blockPosition()) <= 96 * 96) {
                        p.sendSystemMessage(Component.literal("§c§l" + a.boss.title + " is reborn!"));
                    }
                }
            }
        }
        // Reset the ability clock so the first post-channel attack is telegraphed cleanly.
        a.nextAbilityMs = a.transitionUntilMs + 1_000L;
        a.pendingAbility = -1;
    }

    // ════════════════════════════════════════════════════════════
    // ABILITIES (telegraphed: windup on one cycle, execute the next)
    // ════════════════════════════════════════════════════════════

    private static void handleAbilities(ServerLevel level, ActiveBoss a, LivingEntity boss, long now, double max) {
        if (a.pendingAbility >= 0) {
            if (now >= a.windupUntilMs) {
                executeAbility(level, a, boss, a.pendingAbility, max);
                a.pendingAbility = -1;
            }
            return;
        }
        if (now < a.nextAbilityMs) return;

        // Pick an ability by mechanic (some mechanics have a couple).
        int ability = pickAbility(a);
        a.pendingAbility = ability;
        a.windupUntilMs = now + 1_000L; // 1s telegraph
        long cd = a.enraged ? 4_000L : 6_000L;
        a.nextAbilityMs = now + cd;

        telegraph(level, a, boss, ability);
    }

    private static int pickAbility(ActiveBoss a) {
        return switch (a.boss.mechanic) {
            case REGENERATOR -> 0; // bloodrite
            case BLITZER -> 1;     // blink strike
            case SUMMONER -> 2;    // spawn brood
            case AREA_DENIAL -> 3; // burning ground
            case COUNTER -> 4;     // ground slam
            case TRANSFORMER -> RNG.nextBoolean() ? 5 : 4; // cleave / slam
        };
    }

    private static void telegraph(ServerLevel level, ActiveBoss a, LivingEntity boss, int ability) {
        level.playSound(null, boss.blockPosition(), SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.HOSTILE, 1.2f, 0.9f);
        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, boss.getX(), boss.getY() + 2.2, boss.getZ(),
                6, 0.5, 0.3, 0.5, 0.0);
        String warn = switch (ability) {
            case 0 -> "§a⚠ " + a.boss.title + " channels Bloodrite!";
            case 1 -> "§5⚠ " + a.boss.title + " prepares to blink!";
            case 2 -> "§d⚠ " + a.boss.title + " calls the brood!";
            case 3 -> "§6⚠ The ground begins to smoulder — MOVE!";
            case 4 -> "§c⚠ " + a.boss.title + " winds up a slam!";
            default -> "§4⚠ " + a.boss.title + " raises its blade!";
        };
        for (ServerPlayer p : level.players()) {
            if (p.blockPosition().distSqr(boss.blockPosition()) <= 40 * 40) {
                p.sendSystemMessage(Component.literal(warn));
            }
        }
    }

    private static void executeAbility(ServerLevel level, ActiveBoss a, LivingEntity boss, int ability, double max) {
        boolean inDomain = DomainManager.hasDomain(a.entityUuid);
        double dmgScale = (a.enraged ? 1.4 : 1.0) * (inDomain ? 1.5 : 1.0);
        List<ServerPlayer> targets = playersWithin(level, boss, 16.0);

        switch (ability) {
            case 0 -> { // Bloodrite: heal + poison nearby
                boss.heal((float) (max * 0.04));
                for (ServerPlayer p : targets) {
                    p.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1, false, true, true));
                    hurt(level, p, (float) (6 * dmgScale));
                }
                level.sendParticles(ParticleTypes.ITEM_SLIME, boss.getX(), boss.getY() + 1, boss.getZ(),
                        20, 1.2, 0.8, 1.2, 0.1);
            }
            case 1 -> { // Blink strike
                ServerPlayer victim = targets.isEmpty() ? nearestPlayer(level, boss, 32.0)
                        : targets.get(RNG.nextInt(targets.size()));
                if (victim != null) {
                    BlockPos behind = victim.blockPosition();
                    boss.teleportTo(behind.getX() + 0.5, behind.getY(), behind.getZ() + 0.5);
                    hurt(level, victim, (float) (16 * dmgScale));
                    Vec3 kb = new Vec3(victim.getX() - boss.getX(), 0.3, victim.getZ() - boss.getZ())
                            .normalize().scale(1.0);
                    victim.push(kb.x, 0.4, kb.z);
                    level.sendParticles(ParticleTypes.PORTAL, boss.getX(), boss.getY() + 1, boss.getZ(),
                            30, 0.6, 1.0, 0.6, 0.4);
                    level.playSound(null, boss.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                            SoundSource.HOSTILE, 1.0f, 0.8f);
                }
            }
            case 2 -> { // Spawn brood
                int live = countAdds(level, a);
                int toSpawn = Math.min(a.enraged ? 4 : 3, MAX_ADDS - live);
                for (int i = 0; i < toSpawn; i++) {
                    spawnAdd(level, a, boss);
                }
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, boss.getX(), boss.getY() + 1, boss.getZ(),
                        20, 1.5, 1.0, 1.5, 0.05);
            }
            case 3 -> { // Burning ground — damages players who stood in the telegraphed zone
                for (ServerPlayer p : playersWithin(level, boss, 18.0)) {
                    p.setRemainingFireTicks(80);
                    hurt(level, p, (float) (10 * dmgScale));
                    level.sendParticles(ParticleTypes.FLAME, p.getX(), p.getY() + 0.2, p.getZ(),
                            12, 0.4, 0.1, 0.4, 0.02);
                }
                level.playSound(null, boss.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.2f, 0.7f);
            }
            case 4 -> { // Ground slam: shockwave knockback + damage
                for (ServerPlayer p : targets) {
                    hurt(level, p, (float) (12 * dmgScale));
                    Vec3 kb = new Vec3(p.getX() - boss.getX(), 0, p.getZ() - boss.getZ())
                            .normalize().scale(1.2);
                    p.push(kb.x, 0.6, kb.z);
                }
                level.sendParticles(ParticleTypes.EXPLOSION, boss.getX(), boss.getY() + 0.5, boss.getZ(),
                        6, 2.0, 0.2, 2.0, 0.0);
                level.playSound(null, boss.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
                        SoundSource.HOSTILE, 1.0f, 0.8f);
            }
            default -> { // Cleave: arc of heavy damage
                for (ServerPlayer p : playersWithin(level, boss, 6.0)) {
                    hurt(level, p, (float) (18 * dmgScale));
                }
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, boss.getX(), boss.getY() + 1, boss.getZ(),
                        8, 1.5, 0.5, 1.5, 0.0);
            }
        }
    }

    private static void reversalShockwave(ServerLevel level, ActiveBoss a, LivingEntity boss) {
        for (ServerPlayer p : playersWithin(level, boss, 12.0)) {
            hurt(level, p, 10f);
            Vec3 kb = new Vec3(p.getX() - boss.getX(), 0, p.getZ() - boss.getZ()).normalize().scale(1.4);
            p.push(kb.x, 0.5, kb.z);
            p.sendSystemMessage(Component.literal("§c⊘ REVERSAL — your blow is turned back on you!"));
        }
        level.sendParticles(ParticleTypes.CRIT, boss.getX(), boss.getY() + 1, boss.getZ(),
                30, 1.5, 1.0, 1.5, 0.3);
        level.playSound(null, boss.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.8f, 1.4f);
    }

    private static void applyDomainEffects(ServerLevel level, ActiveBoss a, LivingEntity boss, long now) {
        if (!DomainManager.hasDomain(a.entityUuid)) return;
        if (now - a.lastSignatureMs < 2_000L) return;
        a.lastSignatureMs = now;
        var theme = a.boss.theme;
        for (ServerPlayer p : level.players()) {
            if (!DomainManager.isInside(a.entityUuid, p.blockPosition())) continue;
            p.addEffect(new MobEffectInstance(theme.signature, 60, theme.signatureAmp, false, true, true));
            level.sendParticles(theme.particle, p.getX(), p.getY() + 1, p.getZ(),
                    6, 0.4, 0.6, 0.4, 0.02);
        }
    }

    // ════════════════════════════════════════════════════════════
    // ADDS
    // ════════════════════════════════════════════════════════════

    private static void spawnAdd(ServerLevel level, ActiveBoss a, LivingEntity boss) {
        Entity e = EntityType.VEX.spawn(level, boss.blockPosition().above(), EntitySpawnReason.EVENT);
        if (a.boss.mechanic != SpecialGradeBoss.Mechanic.SUMMONER) {
            // Non-summoners get sturdier melee adds.
            if (e != null) e.discard();
            e = EntityType.ZOMBIFIED_PIGLIN.spawn(level, boss.blockPosition().above(), EntitySpawnReason.EVENT);
        }
        if (!(e instanceof Mob add)) {
            if (e != null) e.discard();
            return;
        }
        double x = boss.getX() + (RNG.nextDouble() - 0.5) * 5;
        double z = boss.getZ() + (RNG.nextDouble() - 0.5) * 5;
        add.teleportTo(x, boss.getY(), z);
        var hp = add.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (hp != null) {
            hp.setBaseValue(40.0);
            add.setHealth(40.0f);
        }
        add.setCustomName(Component.literal("§7Cursed Spawn"));
        add.setPersistenceRequired();
        add.addTag(ADD_TAG);
        add.addTag(addTag(a));
        ServerPlayer near = nearestPlayer(level, boss, 32.0);
        if (near != null) add.setTarget(near);
    }

    private static String addTag(ActiveBoss a) {
        return ADD_TAG + "_" + a.entityUuid.toString().substring(0, 8);
    }

    private static int countAdds(ServerLevel level, ActiveBoss a) {
        String tag = addTag(a);
        return level.getEntitiesOfClass(Mob.class, boxAround(a.arena, 40),
                m -> m.isAlive() && m.entityTags().contains(tag)).size();
    }

    private static void clearAdds(ServerLevel level, ActiveBoss a) {
        String tag = addTag(a);
        for (Mob m : level.getEntitiesOfClass(Mob.class, boxAround(a.arena, 48),
                m -> m.entityTags().contains(tag))) {
            m.discard();
        }
    }

    // ════════════════════════════════════════════════════════════
    // DEATH / REWARD / CLEANUP
    // ════════════════════════════════════════════════════════════

    /** Wire this into the AFTER_DEATH event: the primary, fair reward path. */
    public static void onEntityDeath(Entity entity, DamageSource src) {
        if (entity == null) return;
        if (!entity.entityTags().contains(BOSS_TAG)) return;
        ActiveBoss a = ACTIVE.get(entity.getUUID());
        if (a == null) return;
        ServerPlayer killer = null;
        if (src != null && src.getEntity() instanceof ServerPlayer sp) killer = sp;
        onBossKilled((ServerLevel) entity.level(), a, killer);
    }

    private static void onBossKilled(ServerLevel level, ActiveBoss a, ServerPlayer killer) {
        if (a.rewarded) return;
        a.rewarded = true;

        MinecraftServer server = level.getServer();
        DomainManager.revert(level, a.entityUuid);
        clearAdds(level, a);
        a.bar.removeAllPlayers();
        ACTIVE.remove(a.entityUuid);

        // Primary = killer, else summoner if online, else any nearby participant.
        ServerPlayer summoner = onlinePlayer(server, a.summonerUuid);
        ServerPlayer primary = killer != null ? killer : summoner;
        if (primary == null) {
            for (UUID u : a.participants) {
                ServerPlayer p = onlinePlayer(server, u.toString());
                if (p != null) { primary = p; break; }
            }
        }

        List<ServerPlayer> recipients = new ArrayList<>();
        for (UUID u : a.participants) {
            ServerPlayer p = onlinePlayer(server, u.toString());
            if (p != null) recipients.add(p);
        }
        if (killer != null && !recipients.contains(killer)) recipients.add(killer);

        for (ServerPlayer p : recipients) {
            boolean isPrimary = p == primary;
            long coins = isPrimary ? a.boss.coinReward : (long) (a.boss.coinReward * 0.2);
            CoinManager.giveCoins(p, coins);
            giveOrDrop(p, BossRewards.createTrophy(a.boss, 1));

            if (isPrimary || RNG.nextDouble() < 0.25) {
                giveOrDrop(p, BossRewards.createGear(a.boss, 1));
                p.sendSystemMessage(Component.literal("§d★ Relic obtained: §f"
                        + BossRewards.gearName(a.boss)));
            }

            p.sendSystemMessage(Component.literal("§6═══════════════════════════════"));
            p.sendSystemMessage(Component.literal("§4§l☠ SPECIAL GRADE DEFEATED ☠"));
            p.sendSystemMessage(Component.literal("§7" + a.boss.title
                    + (isPrimary ? " §7— §aYou struck the killing blow." : " §7— §7you assisted.")));
            p.sendSystemMessage(Component.literal("§6+" + String.format("%,d", coins) + " coins §7and a trophy."));
            p.sendSystemMessage(Component.literal("§6═══════════════════════════════"));
        }

        if (primary != null) {
            String uuid = primary.getUUID().toString();
            KILLS.computeIfAbsent(uuid, k -> new HashMap<>()).merge(a.boss.id, 1, Integer::sum);
        }

        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, a.arena.getX() + 0.5, a.arena.getY() + 1,
                a.arena.getZ() + 0.5, 40, 1.5, 1.5, 1.5, 0.4);
        level.playSound(null, a.arena, SoundEvents.WITHER_DEATH, SoundSource.HOSTILE, 1.2f, 1.0f);
        save();
    }

    /** End a fight with no payout (abandoned, timed out, errored). */
    private static void cleanupNoReward(MinecraftServer server, ActiveBoss a) {
        ServerLevel level = levelFor(server, a.dim);
        if (level != null) {
            Entity e = level.getEntity(a.entityUuid);
            if (e != null) e.discard();
            clearAdds(level, a);
            DomainManager.revert(level, a.entityUuid);
        }
        a.bar.removeAllPlayers();
        ACTIVE.remove(a.entityUuid);
        save();
    }

    // ════════════════════════════════════════════════════════════
    // QUERIES (for GUI / commands)
    // ════════════════════════════════════════════════════════════

    public static long cooldownRemaining(String uuid) {
        Long until = COOLDOWNS.get(uuid);
        return until == null ? 0L : Math.max(0L, until - System.currentTimeMillis());
    }

    public static int killsOf(String uuid, SpecialGradeBoss boss) {
        Map<String, Integer> m = KILLS.get(uuid);
        return m == null ? 0 : m.getOrDefault(boss.id, 0);
    }

    public static int totalKills(String uuid) {
        Map<String, Integer> m = KILLS.get(uuid);
        return m == null ? 0 : m.values().stream().mapToInt(Integer::intValue).sum();
    }

    public static boolean hasTrophy(String uuid, SpecialGradeBoss boss) {
        return killsOf(uuid, boss) > 0;
    }

    public static int activeCount() {
        return ACTIVE.size();
    }

    /** Admin: clear one player's summon cooldown. */
    public static void clearCooldown(String uuid) {
        COOLDOWNS.remove(uuid);
        save();
    }

    // ════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════

    private static void hurt(ServerLevel level, ServerPlayer p, float amt) {
        p.hurtServer(level, p.damageSources().magic(), amt);
    }

    private static void giveOrDrop(ServerPlayer p, ItemStack s) {
        if (!p.addItem(s)) p.drop(s, false);
    }

    private static ServerPlayer nearestPlayer(ServerLevel level, LivingEntity from, double range) {
        ServerPlayer best = null;
        double bestSq = range * range;
        for (ServerPlayer p : level.players()) {
            if (p.isSpectator() || !p.isAlive()) continue;
            double d = p.distanceToSqr(from);
            if (d <= bestSq) { bestSq = d; best = p; }
        }
        return best;
    }

    private static List<ServerPlayer> playersWithin(ServerLevel level, LivingEntity from, double range) {
        List<ServerPlayer> out = new ArrayList<>();
        double rSq = range * range;
        for (ServerPlayer p : level.players()) {
            if (p.isSpectator() || !p.isAlive()) continue;
            if (p.distanceToSqr(from) <= rSq) out.add(p);
        }
        return out;
    }

    private static ServerPlayer onlinePlayer(MinecraftServer server, String uuid) {
        if (server == null || uuid == null) return null;
        try {
            return server.getPlayerList().getPlayer(UUID.fromString(uuid));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static ServerLevel levelFor(MinecraftServer server, String dim) {
        if (server == null || dim == null) return null;
        for (ServerLevel l : server.getAllLevels()) {
            if (l.dimension().identifier().toString().equals(dim)) return l;
        }
        return null;
    }

    private static net.minecraft.world.phys.AABB boxAround(BlockPos c, double r) {
        return new net.minecraft.world.phys.AABB(
                c.getX() - r, c.getY() - r, c.getZ() - r,
                c.getX() + r, c.getY() + r, c.getZ() + r);
    }
}
