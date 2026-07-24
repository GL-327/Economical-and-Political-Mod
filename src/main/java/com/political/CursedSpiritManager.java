package com.political;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * V6.1–V6.3 — the Cursed Spirits PVE threat layer.
 *
 * <p>Periodically spawns graded Cursed Spirits (dressed vanilla mobs) near active players,
 * drives their per-grade aura flavour from the server tick, and on death awards Cursed Energy,
 * rolls drops, and advances the killer's Curse Hunter rank.</p>
 *
 * <h2>Anti-AFK / anti-farm caps (all enforced below)</h2>
 * <ul>
 *   <li><b>Per-player cap:</b> at most {@link #PER_PLAYER_CAP} spirits alive within
 *       {@link #SPAWN_CAP_RADIUS} blocks of any one player.</li>
 *   <li><b>Per-player spawn interval:</b> a given player only anchors a new spirit at most once
 *       every {@link #PER_PLAYER_INTERVAL_MS} ms, so standing still cannot accelerate the flow.</li>
 *   <li><b>Global soft cap:</b> the wave stops once {@link #GLOBAL_CAP} spirits are already loaded
 *       near players.</li>
 *   <li><b>Kill credit:</b> Cursed Energy is only paid when a spirit is killed by a player; deaths
 *       by fall/lava/other mobs pay nothing, so no passive trap farm.</li>
 *   <li>Cursed Energy is a NEW currency — spirits never mint coins.</li>
 * </ul>
 */
public final class CursedSpiritManager {

    private CursedSpiritManager() {}

    // ── Identity tags ─────────────────────────────────────────────────────────
    public static final String SPIRIT_TAG = "political_cursed";

    // ── Tuning ────────────────────────────────────────────────────────────────
    private static final int SPAWN_INTERVAL_TICKS = 100;   // attempt a wave every 5s
    private static final int AURA_INTERVAL_TICKS  = 20;    // drive auras every 1s

    private static final int PER_PLAYER_CAP        = 5;
    private static final int GLOBAL_CAP            = 60;
    private static final long PER_PLAYER_INTERVAL_MS = 40_000L;
    private static final int SPAWN_CAP_RADIUS      = 48;
    private static final int GLOBAL_SCAN_RADIUS    = 64;
    private static final int AURA_RADIUS           = 12;

    private static final int SPAWN_MIN_RADIUS = 16;
    private static final int SPAWN_MAX_RADIUS = 34;

    /** Base chance (out of 100) that a wave even considers a given eligible player. */
    private static final int WAVE_PLAYER_CHANCE = 45;

    private static boolean enabled = true;
    private static boolean registered = false;
    private static int spawnCounter = 0;
    private static int auraCounter = 0;

    /** Per-player earliest next spawn time (anti-spam). */
    private static final Map<String, Long> nextSpawnAt = new HashMap<>();

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    public static void initialize(Path worldRoot) {
        CursedEnergyManager.initialize(worldRoot);
        register();
    }

    /** Installs the death hook once. Fabric events are multicast, so this is additive. */
    public static void register() {
        if (registered) return;
        registered = true;
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            try {
                if (entity instanceof ServerPlayer) return;
                if (!(entity.level() instanceof ServerLevel level)) return;
                if (!isSpirit(entity)) return;
                Entity killer = damageSource.getEntity();
                onSpiritDeath(level, entity, killer);
            } catch (Throwable t) {
                PoliticalServer.LOGGER.warn("[Cursed] Death handling failed", t);
            }
        });
    }

    public static boolean isEnabled() { return enabled; }

    public static void setEnabled(boolean v) { enabled = v; }

    // ── Identification ────────────────────────────────────────────────────────
    public static boolean isSpirit(Entity e) {
        return e != null && e.entityTags().contains(SPIRIT_TAG);
    }

    private static CursedGrade gradeOf(Entity e) {
        for (String tag : e.entityTags()) {
            CursedGrade g = CursedGrade.fromTag(tag);
            if (g != null) return g;
        }
        return null;
    }

    private static String archIdOf(Entity e) {
        for (String tag : e.entityTags()) {
            if (tag.startsWith("political_arch_")) {
                return tag.substring("political_arch_".length());
            }
        }
        return null;
    }

    // ── Tick ──────────────────────────────────────────────────────────────────
    public static void tick(MinecraftServer server) {
        if (server == null || !enabled) return;

        if (++auraCounter >= AURA_INTERVAL_TICKS) {
            auraCounter = 0;
            try { driveAuras(server); } catch (Throwable t) {
                PoliticalServer.LOGGER.warn("[Cursed] Aura tick failed", t);
            }
        }

        if (++spawnCounter >= SPAWN_INTERVAL_TICKS) {
            spawnCounter = 0;
            try { spawnWave(server); } catch (Throwable t) {
                PoliticalServer.LOGGER.warn("[Cursed] Spawn wave failed", t);
            }
        }
    }

    // ── Spawning ──────────────────────────────────────────────────────────────
    private static void spawnWave(MinecraftServer server) {
        List<ServerPlayer> players = new ArrayList<>();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (p.isSpectator()) continue;
            if (!(p.level() instanceof ServerLevel)) continue;
            if (!isSpawnDimension((ServerLevel) p.level())) continue;
            players.add(p);
        }
        if (players.isEmpty()) return;

        // Approximate the global live count first.
        Set<UUID> seen = new HashSet<>();
        for (ServerPlayer p : players) {
            ServerLevel level = (ServerLevel) p.level();
            for (Mob m : nearbySpirits(level, p.blockPosition(), GLOBAL_SCAN_RADIUS)) {
                seen.add(m.getUUID());
            }
        }
        int globalCount = seen.size();

        long now = System.currentTimeMillis();
        for (ServerPlayer anchor : players) {
            if (globalCount >= GLOBAL_CAP) break;

            String uuid = anchor.getUUID().toString();
            if (now < nextSpawnAt.getOrDefault(uuid, 0L)) continue;

            ServerLevel level = (ServerLevel) anchor.level();
            int perPlayer = nearbySpirits(level, anchor.blockPosition(), SPAWN_CAP_RADIUS).size();
            if (perPlayer >= PER_PLAYER_CAP) continue;

            if (level.getRandom().nextInt(100) >= WAVE_PLAYER_CHANCE) continue;

            Mob spawned = spawnNear(level, anchor);
            if (spawned != null) {
                globalCount++;
                nextSpawnAt.put(uuid, now + PER_PLAYER_INTERVAL_MS);
            }
        }
    }

    private static Mob spawnNear(ServerLevel level, ServerPlayer anchor) {
        int danger = regionDanger(level, anchor.blockPosition());

        CursedGrade grade = rollGrade(level, danger);
        if (grade == null) return null;
        List<CursedSpirit> pool = CursedSpirit.ofGrade(grade);
        if (pool.isEmpty()) return null;
        CursedSpirit arch = pool.get(level.getRandom().nextInt(pool.size()));

        double angle = level.getRandom().nextDouble() * Math.PI * 2.0;
        int radius = SPAWN_MIN_RADIUS + level.getRandom().nextInt(SPAWN_MAX_RADIUS - SPAWN_MIN_RADIUS + 1);
        int x = (int) Math.round(anchor.getX() + Math.cos(angle) * radius);
        int z = (int) Math.round(anchor.getZ() + Math.sin(angle) * radius);
        int y;
        try {
            BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(x, (int) anchor.getY(), z));
            y = ground.getY();
        } catch (Throwable t) {
            y = (int) anchor.getY();
        }
        if (y < level.getMinY() + 2) y = (int) anchor.getY();
        BlockPos pos = new BlockPos(x, y, z);

        Entity spawned;
        try {
            spawned = arch.baseType().spawn(level, pos, EntitySpawnReason.EVENT);
        } catch (Throwable t) {
            PoliticalServer.LOGGER.warn("[Cursed] Failed to spawn {}: {}", arch.id(), t.toString());
            return null;
        }
        if (!(spawned instanceof Mob mob)) {
            if (spawned != null) spawned.discard();
            return null;
        }

        dress(level, mob, arch, grade, danger);
        announce(level, anchor, arch, grade);
        return mob;
    }

    private static void dress(ServerLevel level, Mob mob, CursedSpirit arch, CursedGrade grade, int danger) {
        double regionScale = 1.0 + 0.08 * danger;
        double seasonScale = seasonScale();

        double health = grade.baseHealth() * regionScale * seasonScale;
        double damage = grade.baseDamage() * regionScale;

        var hp = mob.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) {
            hp.setBaseValue(health);
            mob.setHealth((float) health);
        }
        var atk = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) atk.setBaseValue(damage);
        var kb = mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kb != null) kb.setBaseValue(Math.min(1.0, kb.getBaseValue() + 0.15 + 0.06 * grade.ordinal()));
        var speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && grade.ordinal() >= CursedGrade.GRADE_2.ordinal()) {
            speed.setBaseValue(speed.getBaseValue() * 1.12);
        }

        mob.addTag(SPIRIT_TAG);
        mob.addTag(grade.tag());
        mob.addTag(arch.archTag());
        mob.setPersistenceRequired();

        mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 999999, 0, true, false, false));
        if (grade.ordinal() >= CursedGrade.GRADE_1.ordinal()) {
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 999999, 0, true, false, false));
        }

        equip(mob, grade);

        String symbol = grade == CursedGrade.SPECIAL_GRADE ? "☠ " : "✦ ";
        mob.setCustomName(Component.literal(symbol + arch.name() + " §8[" + grade.label + "]")
                .withStyle(grade.color, ChatFormatting.BOLD));
        mob.setCustomNameVisible(true);
    }

    private static void equip(Mob mob, CursedGrade grade) {
        ItemStack weapon;
        switch (grade) {
            case GRADE_4 -> weapon = null;
            case GRADE_3 -> weapon = new ItemStack(Items.STONE_SWORD);
            case GRADE_2 -> weapon = new ItemStack(Items.IRON_SWORD);
            case GRADE_1 -> weapon = new ItemStack(Items.DIAMOND_SWORD);
            case SPECIAL_GRADE -> weapon = new ItemStack(Items.NETHERITE_SWORD);
            default -> weapon = null;
        }
        try {
            if (weapon != null) {
                mob.setItemSlot(EquipmentSlot.MAINHAND, weapon);
                mob.setDropChance(EquipmentSlot.MAINHAND, 0f);
            }
        } catch (Throwable ignored) {
            // Some dressed types (phantom, witch) have no hand slot; harmless.
        }
    }

    /** Weighted grade roll, filtered by the region-danger gate. */
    private static CursedGrade rollGrade(ServerLevel level, int danger) {
        int total = 0;
        for (CursedGrade g : CursedGrade.values()) {
            if (danger >= g.minRegionDanger) total += g.spawnWeight;
        }
        if (total <= 0) return CursedGrade.GRADE_4;
        int roll = level.getRandom().nextInt(total);
        for (CursedGrade g : CursedGrade.values()) {
            if (danger < g.minRegionDanger) continue;
            roll -= g.spawnWeight;
            if (roll < 0) return g;
        }
        return CursedGrade.GRADE_4;
    }

    // ── Aura flavour ───────────────────────────────────────────────────────────
    private static void driveAuras(MinecraftServer server) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (p.isSpectator()) continue;
            if (!(p.level() instanceof ServerLevel level)) continue;
            List<Mob> spirits = nearbySpirits(level, p.blockPosition(), AURA_RADIUS);
            if (spirits.isEmpty()) continue;
            double hunterResist = CursedEnergyManager.passiveBonus(p.getUUID().toString());
            for (Mob spirit : spirits) {
                CursedGrade grade = gradeOf(spirit);
                CursedSpirit arch = CursedSpirit.byId(archIdOf(spirit));
                if (grade == null || arch == null) continue;
                applyAura(level, spirit, p, grade, arch.ability(), hunterResist);
            }
        }
    }

    private static void applyAura(ServerLevel level, Mob spirit, ServerPlayer target,
                                  CursedGrade grade, CursedSpirit.Ability ability, double hunterResist) {
        int amp = Math.min(2, grade.ordinal() / 2);
        int base = 60; // 3s, reapplied each second
        int dur = (int) Math.round(base * (1.0 - hunterResist)); // hunters shrug off curses a little

        switch (ability) {
            case ROT_AURA -> {
                affect(target, MobEffects.HUNGER, dur, 0);
                if (level.getRandom().nextInt(4) == 0) affect(target, MobEffects.WEAKNESS, dur, 0);
            }
            case WEB_SNARE -> affect(target, MobEffects.SLOWNESS, dur, Math.min(1, amp));
            case VENOM -> affect(target, MobEffects.POISON, dur, 0);
            case CHILL -> {
                affect(target, MobEffects.WEAKNESS, dur, 0);
                affect(target, MobEffects.MINING_FATIGUE, dur, 0);
            }
            case CHOKE -> {
                affect(target, MobEffects.SLOWNESS, dur, amp);
                if (level.getRandom().nextInt(3) == 0) affect(target, MobEffects.NAUSEA, 40, 0);
            }
            case DECAY -> affect(target, MobEffects.WITHER, dur, 0);
            case DREAD -> {
                if (level.getRandom().nextInt(4) == 0) affect(target, MobEffects.DARKNESS, 60, 0);
            }
            case PLAGUE -> {
                affect(target, MobEffects.HUNGER, dur, 0);
                affect(target, MobEffects.POISON, dur, 0);
            }
            case WITHER_FLAME -> affect(target, MobEffects.WITHER, dur, amp);
            case GAZE -> {
                affect(target, MobEffects.DARKNESS, dur, 0);
                if (level.getRandom().nextInt(6) == 0) affect(target, MobEffects.LEVITATION, 20, 0);
            }
            case QUAKE -> {
                affect(target, MobEffects.MINING_FATIGUE, dur, amp);
                if (level.getRandom().nextInt(5) == 0) knockback(spirit, target);
            }
            case DOMAIN -> {
                affect(target, MobEffects.WEAKNESS, dur, amp);
                spirit.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 100, 0, true, false, false));
                if (level.getRandom().nextInt(6) == 0) blink(level, spirit, target);
            }
            case CATACLYSM -> {
                affect(target, MobEffects.WITHER, dur, 1);
                affect(target, MobEffects.MINING_FATIGUE, dur, 1);
                affect(target, MobEffects.DARKNESS, dur, 0);
                spirit.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 100, 1, true, false, false));
                if (level.getRandom().nextInt(8) == 0) {
                    knockback(spirit, target);
                    level.playSound(null, spirit.blockPosition(), SoundEvents.WARDEN_ROAR,
                            SoundSource.HOSTILE, 1.2f, 0.7f);
                }
                if (level.getRandom().nextInt(10) == 0) blink(level, spirit, target);
            }
        }

        // Cheap ambient particle so spirits read as "off".
        try {
            Vec3 c = spirit.getBoundingBox().getCenter();
            level.sendParticles(grade == CursedGrade.SPECIAL_GRADE ? ParticleTypes.SCULK_SOUL : ParticleTypes.SOUL,
                    c.x, c.y + 0.3, c.z, 3, 0.3, 0.4, 0.3, 0.01);
        } catch (Throwable ignored) {}
    }

    private static void affect(ServerPlayer p, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
                               int dur, int amp) {
        if (dur <= 0) return;
        p.addEffect(new MobEffectInstance(effect, dur, Math.max(0, amp), true, true, true));
    }

    private static void knockback(Mob from, ServerPlayer target) {
        Vec3 dir = target.position().subtract(from.position()).normalize();
        target.setDeltaMovement(target.getDeltaMovement().add(dir.x * 0.6, 0.42, dir.z * 0.6));
        target.hurtMarked = true;
    }

    private static void blink(ServerLevel level, Mob spirit, ServerPlayer target) {
        Vec3 behind = target.position().subtract(target.getLookAngle().scale(2.0));
        spirit.teleportTo(behind.x, target.getY(), behind.z);
        try {
            level.sendParticles(ParticleTypes.PORTAL, behind.x, behind.y + 1, behind.z, 20, 0.4, 0.6, 0.4, 0.2);
            level.playSound(null, spirit.blockPosition(), SoundEvents.PORTAL_TRAVEL,
                    SoundSource.HOSTILE, 0.5f, 1.4f);
        } catch (Throwable ignored) {}
    }

    // ── Death & reward ────────────────────────────────────────────────────────
    private static void onSpiritDeath(ServerLevel level, LivingEntity spirit, Entity killerEntity) {
        CursedGrade grade = gradeOf(spirit);
        if (grade == null) return;
        String archId = archIdOf(spirit);
        CursedSpirit arch = CursedSpirit.byId(archId);
        BlockPos pos = spirit.blockPosition();

        // Death throes.
        try {
            Vec3 c = spirit.getBoundingBox().getCenter();
            level.sendParticles(ParticleTypes.SCULK_SOUL, c.x, c.y + 0.4, c.z, 24, 0.4, 0.6, 0.4, 0.03);
            level.sendParticles(ParticleTypes.SMOKE, c.x, c.y + 0.4, c.z, 16, 0.3, 0.4, 0.3, 0.02);
        } catch (Throwable ignored) {}

        if (!(killerEntity instanceof ServerPlayer killer)) {
            return; // No player credit → no reward. Anti-trap-farm.
        }

        int danger = regionDanger(level, pos);
        double regionScale = 1.0 + 0.08 * danger;
        double bonus = CursedEnergyManager.passiveBonus(killer.getUUID().toString());
        long energy = Math.round(grade.cursedEnergyReward * regionScale * (1.0 + bonus));

        CursedEnergyManager.earn(killer, energy);
        CursedEnergyManager.recordKill(killer, grade);
        boolean firstTime = arch != null && CursedEnergyManager.markEncountered(killer, archId);

        rollDrops(killer, level, grade, pos);

        // A little magic progression for exorcising a curse.
        try { MagicManager.addXp(killer, magicXpFor(grade)); } catch (Throwable ignored) {}

        killer.sendSystemMessage(Component.literal("☠ Exorcised ")
                .withStyle(ChatFormatting.DARK_PURPLE)
                .append(Component.literal(arch != null ? arch.name() : "a Cursed Spirit")
                        .withStyle(grade.color, ChatFormatting.BOLD))
                .append(Component.literal(" §7(+§d" + energy + " Cursed Energy§7)")));
        if (firstTime) {
            killer.sendSystemMessage(Component.literal("§8✧ New curse recorded in your codex: §7"
                    + arch.name()));
        }
        CursedEnergyManager.HunterRank before = CursedEnergyManager.HunterRank.forKills(
                CursedEnergyManager.lifetimeSlain(killer.getUUID().toString()) - 1);
        CursedEnergyManager.HunterRank after = CursedEnergyManager.rank(killer.getUUID().toString());
        if (after != before) {
            killer.sendSystemMessage(Component.literal("§5✦ Curse Hunter rank up: §d" + after.label
                    + " §7(+" + after.bonusPercent + "% vs spirits)"));
            try {
                level.playSound(null, killer.blockPosition(), SoundEvents.WITHER_SPAWN,
                        SoundSource.PLAYERS, 0.6f, 1.3f);
            } catch (Throwable ignored) {}
        }

        if (grade == CursedGrade.SPECIAL_GRADE) {
            for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
                p.sendSystemMessage(Component.literal("§4☠ §cA Special Grade curse has been exorcised by §f"
                        + killer.getName().getString() + "§c."));
            }
        }
    }

    private static void rollDrops(ServerPlayer killer, ServerLevel level, CursedGrade grade, BlockPos pos) {
        var rng = level.getRandom();

        // Cursed Energy Shards: guaranteed, scaling gently with grade.
        int shards = 1 + rng.nextInt(1 + grade.ordinal());
        for (int i = 0; i < shards; i++) giveOrDrop(killer, CursedItems.createShard());

        switch (grade) {
            case GRADE_4 -> {
                if (rng.nextInt(3) == 0) giveOrDrop(killer, CursedItems.createResidue());
            }
            case GRADE_3 -> {
                giveOrDrop(killer, CursedItems.createResidue());
                if (rng.nextInt(2) == 0) giveOrDrop(killer, CursedItems.createResidue());
            }
            case GRADE_2 -> {
                giveOrDrop(killer, CursedItems.createResidue());
                if (rng.nextInt(3) == 0) giveOrDrop(killer, CursedItems.createCore());
                if (rng.nextInt(4) == 0) giveOrDrop(killer, CursedItems.createOrb());
            }
            case GRADE_1 -> {
                giveOrDrop(killer, CursedItems.createCore());
                if (rng.nextInt(2) == 0) giveOrDrop(killer, CursedItems.createCore());
                giveOrDrop(killer, CursedItems.createOrb());
            }
            case SPECIAL_GRADE -> {
                giveOrDrop(killer, CursedItems.createRelic());
                for (int i = 0; i < 3; i++) giveOrDrop(killer, CursedItems.createCore());
                for (int i = 0; i < 2; i++) giveOrDrop(killer, CursedItems.createOrb());
            }
        }
    }

    private static int magicXpFor(CursedGrade grade) {
        return switch (grade) {
            case GRADE_4 -> 2;
            case GRADE_3 -> 5;
            case GRADE_2 -> 10;
            case GRADE_1 -> 20;
            case SPECIAL_GRADE -> 40;
        };
    }

    private static void announce(ServerLevel level, ServerPlayer anchor, CursedSpirit arch, CursedGrade grade) {
        if (grade == CursedGrade.SPECIAL_GRADE) {
            for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
                p.sendSystemMessage(Component.literal("§4═══════════════════════════════"));
                p.sendSystemMessage(Component.literal("§4☠ A SPECIAL GRADE CURSE HAS MANIFESTED ☠")
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
                p.sendSystemMessage(Component.literal("§7" + arch.name() + " §8— " + arch.flavour()));
                p.sendSystemMessage(Component.literal("§8Near §7" + anchor.getName().getString()));
                p.sendSystemMessage(Component.literal("§4═══════════════════════════════"));
            }
            try {
                level.playSound(null, anchor.blockPosition(), SoundEvents.WITHER_SPAWN,
                        SoundSource.HOSTILE, 1.6f, 0.6f);
            } catch (Throwable ignored) {}
        } else if (grade.ordinal() >= CursedGrade.GRADE_2.ordinal()) {
            anchor.sendSystemMessage(Component.literal("§8A ")
                    .append(Component.literal(grade.label).withStyle(grade.color, ChatFormatting.BOLD))
                    .append(Component.literal(" §8stirs nearby — " + arch.name() + ".")));
        }
    }

    // ── Region / season helpers ────────────────────────────────────────────────
    /** Region danger 0..9, blending settlement tier with dimension baseline. */
    public static int regionDanger(ServerLevel level, BlockPos pos) {
        int settlement = 0;
        try {
            String dim = level.dimension().identifier().toString();
            Settlement s = SettlementManager.getContaining(dim, pos);
            if (s != null) settlement = s.tierEnum().rank() + 1;
        } catch (Throwable ignored) {}

        int dimBase = 0;
        if (level.dimension() == Level.NETHER) dimBase = 2;
        else if (level.dimension() == Level.END) dimBase = 3;

        return Math.max(settlement, dimBase);
    }

    private static double seasonScale() {
        try {
            if (SeasonManager.current() == Season.WINTER) return 1.25;
        } catch (Throwable ignored) {}
        return 1.0;
    }

    private static boolean isSpawnDimension(ServerLevel level) {
        return true; // overworld, nether and end all host curses (region-gated by grade)
    }

    // ── Scans ───────────────────────────────────────────────────────────────────
    private static List<Mob> nearbySpirits(ServerLevel level, BlockPos center, int radius) {
        AABB box = new AABB(center).inflate(radius);
        return level.getEntitiesOfClass(Mob.class, box, CursedSpiritManager::isSpirit);
    }

    private static void giveOrDrop(ServerPlayer p, ItemStack s) {
        if (s == null || s.isEmpty()) return;
        if (!p.getInventory().add(s)) p.drop(s, false);
    }

    // ── Admin ─────────────────────────────────────────────────────────────────
    /** Force-spawn a specific archetype near a player (used by the admin command). */
    public static boolean adminSpawn(ServerPlayer anchor, String archId) {
        if (!(anchor.level() instanceof ServerLevel level)) return false;
        CursedSpirit arch = CursedSpirit.byId(archId);
        if (arch == null) return false;
        int danger = regionDanger(level, anchor.blockPosition());
        // Reuse the ring-spawn path but pin the archetype.
        double angle = level.getRandom().nextDouble() * Math.PI * 2.0;
        int radius = SPAWN_MIN_RADIUS;
        int x = (int) Math.round(anchor.getX() + Math.cos(angle) * radius);
        int z = (int) Math.round(anchor.getZ() + Math.sin(angle) * radius);
        int y;
        try {
            y = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(x, (int) anchor.getY(), z)).getY();
        } catch (Throwable t) {
            y = (int) anchor.getY();
        }
        BlockPos pos = new BlockPos(x, y, z);
        Entity spawned;
        try {
            spawned = arch.baseType().spawn(level, pos, EntitySpawnReason.EVENT);
        } catch (Throwable t) {
            return false;
        }
        if (!(spawned instanceof Mob mob)) {
            if (spawned != null) spawned.discard();
            return false;
        }
        dress(level, mob, arch, arch.grade(), danger);
        return true;
    }
}
