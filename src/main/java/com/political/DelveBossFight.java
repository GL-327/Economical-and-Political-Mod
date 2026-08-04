package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The floor boss.
 *
 * <p>Three phases, the same shape as the dragon and the wither so a player who has fought those
 * already knows how to read this one: a bar that renames itself, a warning before anything heavy,
 * and a middle phase that asks you to do something other than keep hitting.</p>
 *
 * <p>The boss is sealed until the floor is cleared, which is what makes the rooms worth doing
 * rather than something to run past on the way to the fight.</p>
 */
public final class DelveBossFight {

    private DelveBossFight() {}

    public static final String BOSS_TAG = "political_delve_boss";

    /** What a boss is currently doing. */
    public enum Phase {
        WAKING("Waking", "It has not been disturbed in a long time."),
        CALLING("Calling", "It will not bleed while its court stands."),
        FINAL("Undone", "It has stopped defending itself.");

        public final String display;
        public final String blurb;

        Phase(String display, String blurb) {
            this.display = display;
            this.blurb = blurb;
        }
    }

    private static final class Fight {
        UUID bossId;
        Phase phase = Phase.WAKING;
        ServerBossEvent bar;
        long nextAttack;
        long telegraphAt;
        boolean telegraphed;
        final List<UUID> court = new ArrayList<>();
    }

    private static final Map<UUID, Fight> FIGHTS = new HashMap<>();

    // ── Spawning ──────────────────────────────────────────────────────────

    /** Puts the floor boss in its room and opens its bar. */
    public static void spawn(MinecraftServer server, DelveRun run) {
        ServerLevel level = DelveManager.realm(server);
        if (level == null || run.layout.bossRoom == null) return;

        DelveBuilder.Placed room = run.layout.bossRoom;
        BlockPos at = room.centre();

        Mob boss;
        try {
            var made = run.floor.eliteMob.create(level, EntitySpawnReason.EVENT);
            if (!(made instanceof Mob m)) return;
            boss = m;
        } catch (Exception e) {
            return;
        }

        boss.setPos(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
        boss.setPersistenceRequired();
        boss.addTag(BOSS_TAG);
        boss.addTag(run.id.toString());
        boss.setCustomName(Component.literal(run.floor.bossName)
                .withStyle(run.floor.colour, ChatFormatting.BOLD));
        boss.setCustomNameVisible(true);

        try {
            var hp = boss.getAttribute(Attributes.MAX_HEALTH);
            if (hp != null) {
                hp.setBaseValue(run.floor.bossHealth);
                boss.setHealth(boss.getMaxHealth());
            }
            var dmg = boss.getAttribute(Attributes.ATTACK_DAMAGE);
            if (dmg != null) dmg.setBaseValue(6.0 + run.floor.tier * 3.0);
            var kb = boss.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (kb != null) kb.setBaseValue(1.0);
        } catch (Exception ignored) {
            // Stock attributes are survivable, the fight is just shorter.
        }
        boss.setItemSlot(EquipmentSlot.HEAD, DelvePopulator.head(BOSS_HEAD));
        try {
            boss.setDropChance(EquipmentSlot.HEAD, 0f);
        } catch (Exception ignored) {
            // Cosmetic.
        }
        level.addFreshEntity(boss);

        Fight f = new Fight();
        f.bossId = boss.getUUID();
        f.bar = new ServerBossEvent(UUID.randomUUID(),
                Component.literal(run.floor.bossName).withStyle(run.floor.colour, ChatFormatting.BOLD),
                BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_6);
        f.bar.setProgress(1.0f);
        f.nextAttack = server.getTickCount() + 60;
        FIGHTS.put(run.id, f);

        for (ServerPlayer p : run.online()) {
            f.bar.addPlayer(p);
            p.sendSystemMessage(Component.literal(""));
            p.sendSystemMessage(Component.literal("  " + run.floor.colour + "§l"
                    + run.floor.bossName.toUpperCase(java.util.Locale.ROOT)));
            p.sendSystemMessage(Component.literal("  §7Three stages. Watch the bar."));
            p.sendSystemMessage(Component.literal(""));
            level.playSound(null, p.blockPosition(), SoundEvents.WITHER_SPAWN,
                    SoundSource.HOSTILE, 1.0f, 0.7f);
        }
    }

    private static final String BOSS_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjdmZTRjYWQ4ZDNjMWY4ZTNiNTFmM2VkNGE1YWNmMzRlNGE0MjA3Nzc3ZDNhNzk1YzFkOTRhNGY0ODU1NzgwOSJ9fX0=";

    // ── Running ───────────────────────────────────────────────────────────

    public static void tick(MinecraftServer server, DelveRun run) {
        Fight f = FIGHTS.get(run.id);
        if (f == null) return;
        ServerLevel level = DelveManager.realm(server);
        if (level == null) return;

        var e = level.getEntity(f.bossId);
        if (!(e instanceof Mob boss) || !boss.isAlive()) {
            // Gone from the world. Death is handled by the kill hook, which is the only place that
            // can tell a kill from a despawn, so all this does is close the bar.
            forget(run);
            return;
        }

        for (ServerPlayer p : run.online()) f.bar.addPlayer(p);
        float frac = boss.getHealth() / Math.max(1f, boss.getMaxHealth());
        f.bar.setProgress(Math.max(0f, Math.min(1f, frac)));

        Phase want = frac > 0.66f ? Phase.WAKING : frac > 0.33f ? Phase.CALLING : Phase.FINAL;
        if (want != f.phase) enterPhase(level, run, f, boss, want);

        f.court.removeIf(id -> {
            var c = level.getEntity(id);
            return c == null || !c.isAlive();
        });

        long now = server.getTickCount();
        if (f.telegraphed && now >= f.telegraphAt) {
            f.telegraphed = false;
            resolve(level, run, f, boss);
        } else if (!f.telegraphed && now >= f.nextAttack) {
            f.telegraphed = true;
            f.telegraphAt = now + 35;
            f.nextAttack = f.telegraphAt + (f.phase == Phase.FINAL ? 70 : 120);
            for (ServerPlayer p : run.online()) {
                p.sendSystemMessage(Component.literal(f.phase == Phase.FINAL
                        ? "§4⚠ It is winding up something final."
                        : "§e⚠ It is drawing back."), true);
            }
        }
    }

    private static void enterPhase(ServerLevel level, DelveRun run, Fight f, Mob boss, Phase phase) {
        f.phase = phase;
        f.bar.setName(Component.literal(run.floor.bossName + " — " + phase.display)
                .withStyle(run.floor.colour, ChatFormatting.BOLD));

        for (ServerPlayer p : run.online()) {
            p.sendSystemMessage(Component.literal(""));
            p.sendSystemMessage(Component.literal("  " + run.floor.colour + "§l"
                    + phase.display.toUpperCase(java.util.Locale.ROOT)));
            p.sendSystemMessage(Component.literal("  §7" + phase.blurb));
            p.sendSystemMessage(Component.literal(""));
        }

        if (phase == Phase.CALLING) {
            for (int i = 0; i < 4; i++) {
                try {
                    var made = run.floor.commonMob.create(level, EntitySpawnReason.EVENT);
                    if (!(made instanceof Mob court)) continue;
                    double a = (Math.PI / 2) * i;
                    court.setPos(boss.getX() + Math.cos(a) * 4, boss.getY(),
                            boss.getZ() + Math.sin(a) * 4);
                    court.setPersistenceRequired();
                    court.addTag(DelvePopulator.MOB_TAG);
                    court.addTag(run.id.toString());
                    court.setCustomName(Component.literal("Court of " + run.floor.bossName)
                            .withStyle(run.floor.colour));
                    court.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20 * 600, 1));
                    level.addFreshEntity(court);
                    f.court.add(court.getUUID());
                } catch (Exception ignored) {
                    // One fewer courtier is not worth failing the phase over.
                }
            }
        }
        if (phase == Phase.FINAL) {
            boss.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 600, 1));
            boss.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 20 * 600, 1));
        }
    }

    /**
     * The heavy attack, which is different on every depth.
     *
     * <p>All seven used to do the same thing with a different number in front of it, so beating one
     * taught a player nothing about the next. Each one now asks something of its own: the Catacombs
     * boss raises what you already killed, the Ice Vault pins you where you stand, the End Castle
     * takes the floor away. The damage is the same shape underneath so none of them is a wall.</p>
     */
    private static void resolve(ServerLevel level, DelveRun run, Fight f, Mob boss) {
        float dmg = (f.phase == Phase.FINAL ? 11f : 6f) + run.floor.tier * 1.5f;
        List<ServerPlayer> hit = new ArrayList<>();
        for (ServerPlayer p : run.online()) {
            if (p.distanceToSqr(boss) <= 40 * 40) hit.add(p);
        }

        for (ServerPlayer p : hit) {
            p.hurtServer(level, p.damageSources().mobAttack(boss), dmg);
            signature(level, run, boss, p);
        }
        level.playSound(null, boss.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.HOSTILE, 1.4f, 0.8f);
    }

    /** What this depth's boss does that no other one does. */
    private static void signature(ServerLevel level, DelveRun run, Mob boss, ServerPlayer p) {
        switch (run.floor) {
            case CATACOMBS -> {
                // Raises what the floor already lost, so a cleared room is not a safe one.
                p.sendSystemMessage(Component.literal("§7✦ The dead get up again."), true);
                for (int i = 0; i < 2; i++) {
                    try {
                        var made = net.minecraft.world.entity.EntityType.SKELETON
                                .create(level, EntitySpawnReason.EVENT);
                        if (made instanceof Mob m) {
                            m.setPos(p.getX() + (i == 0 ? 2 : -2), p.getY(), p.getZ());
                            m.setPersistenceRequired();
                            m.addTag(DelvePopulator.MOB_TAG);
                            m.addTag(run.id.toString());
                            level.addFreshEntity(m);
                        }
                    } catch (Exception ignored) {
                        // One fewer skeleton is not worth failing the attack over.
                    }
                }
            }
            case SUNKEN -> {
                p.sendSystemMessage(Component.literal("§3✦ The water closes over you."), true);
                p.setAirSupply(Math.max(0, p.getAirSupply() - 120));
                p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 2));
            }
            case ICE -> {
                p.sendSystemMessage(Component.literal("§b✦ You are held where you stand."), true);
                p.setTicksFrozen(Math.min(p.getTicksFrozen() + 200, 400));
                p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 4));
                p.setDeltaMovement(0, p.getDeltaMovement().y, 0);
            }
            case SANDS -> {
                p.sendSystemMessage(Component.literal("§e✦ The sand takes your eyes."), true);
                p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                p.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 2));
            }
            case TOXIC -> {
                p.sendSystemMessage(Component.literal("§2✦ It is in your lungs."), true);
                p.addEffect(new MobEffectInstance(MobEffects.POISON, 140, 2));
                p.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 120, 0));
            }
            case CITADEL -> {
                p.sendSystemMessage(Component.literal("§6✦ Your arm goes heavy."), true);
                p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1));
                p.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 160, 1));
            }
            case END_CASTLE -> {
                // Takes the floor away, which on a raised arena is the whole problem.
                p.sendSystemMessage(Component.literal("§5✦ The floor stops mattering."), true);
                p.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 50, 1));
                p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0));
            }
        }
    }

    /** How much of a hit the boss takes. Its court shields it, so the court is the answer. */
    public static float incomingScale(DelveRun run) {
        Fight f = FIGHTS.get(run.id);
        if (f == null) return 1f;
        return f.phase == Phase.CALLING && !f.court.isEmpty() ? 0.2f : 1f;
    }

    public static boolean isBoss(LivingEntity e) {
        return e.entityTags().contains(BOSS_TAG);
    }

    public static void onBossDeath(DelveRun run) {
        Fight f = FIGHTS.remove(run.id);
        if (f != null) {
            f.bar.removeAllPlayers();
            if (PoliticalServer.server != null) {
                ServerLevel level = DelveManager.realm(PoliticalServer.server);
                if (level != null) {
                    for (UUID id : f.court) {
                        var c = level.getEntity(id);
                        if (c != null) c.discard();
                    }
                }
            }
        }
        run.bossDead = true;
        DelveManager.end(run, true);
    }

    /** Drops the fight without finishing the run, for when a run is abandoned. */
    public static void forget(DelveRun run) {
        Fight f = FIGHTS.remove(run.id);
        if (f != null) f.bar.removeAllPlayers();
    }
}
